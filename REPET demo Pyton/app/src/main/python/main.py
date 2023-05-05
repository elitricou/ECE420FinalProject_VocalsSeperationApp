import numpy as np
from scipy import signal
from scipy.io.wavfile import read, write

#repet but edited to make mono files more efficient
def main(file_name, min_period=1, max_period=10, cutoff=0.0, power = 2.0):
    Fs, audio_sample = read(file_name)
    background = np.int16(repet(audio_sample, Fs, [min_period, max_period], cutoff, power))
    vocal = audio_sample - background
    #write( file_name.replace('.wav', "_background.wav"), Fs, background)
    write( file_name.replace(".wav", "_enhanced_vocals_5dB.wav"), Fs, vocal)


def repet(audio_sample, Fs, period, cutoff, power):
    
    ###STAGE 1
    samples = np.shape(audio_sample)[0]

    # STFT parameters
    #use a 40 ms data sample for pitch detection with a 50% overlap
    #sample frequency Fs is passed in through the function
    winsize = 2**(int(np.ceil(np.log2( Fs* 0.04))))   #the nearest power of 2 for the window size of the stft
    overlap = winsize/2
    # number of timeframes in the stft
    tframe = int(np.ceil(samples/ overlap)) + 1

    # Initialize stft as array
    stft = np.zeros((winsize, tframe), dtype=complex)
    window = signal.hamming(winsize, sym=False)  #using a hamming window
    
        # stft of each channel
    stft[:, :] = signal.stft(audio_sample[:], Fs, window, winsize, return_onesided=False)[2]


    # absolute value spectrogram
    spectrogram = abs(stft[0 : int(winsize / 2) + 1, :])   #mixture spectrogram V
  
    #beat spectrum b
    # square V (mixed spectrogram) to emphasize appearance of peacks of periodicity  in b
    beat_spectrum = _beatspec(np.power(spectrogram, 2))
            #this is an array to the power, so np.power
    
    newperiod = np.round(np.array(period) *  Fs / overlap).astype(int) # adjust period for sampling frequency
    repeating_period = _periods(beat_spectrum, newperiod) # Estimate the repeating period in time frames given the period range

   
    ####STAGE 2 and 3
    #taking the spectrogram over the repeating period and creating the soft time-frequency mask
      
    newcutoff = round(cutoff * winsize / Fs) #adjust cutoff for sampling frequency
     # Initialize the background signal
    background_signal = np.zeros((samples))
            #keep these outside for loop
    
       # find repeating mask M
    mask = _mask(spectrogram, repeating_period, power)

        # high-pass filtering
    mask[1 : newcutoff + 1, :] = 1

    mask = np.concatenate((mask, mask[-2:0:-1, :]), axis=0)

        # Synthesize the repeating background for the current channel
    background_full = np.real(signal.istft(mask * stft[:, :], Fs, window, winsize, input_onesided=False)[1])  

        # Truncate to original samples value
    background_signal[:] = background_full[0:samples]

    return background_signal

#private functions
def _acorr(data):
    """
    FFT-based autocorrelation 
    In
        float data [rows][columns]
    Out
        float acorr [number_lags][number_columns]
    """

    n_rows = data.shape[0]

    # Compute the power spectrum and inverse fft into autocorrelation
    data = np.power( np.abs( np.fft.fft(data, n=2 * n_rows, axis=0) ), 2 )

    acorr = np.real(np.fft.ifft(data, axis=0))

    # Discard the symmetric part
    acorr = acorr[0:n_rows, :]

    # Derive the unbiased autocorrelation <- need to explain this
    #acorr = np.divide(
    #    acorr, np.arange(n_rows, 0, -1)[:, np.newaxis]
    #)
    return acorr

def _beatspec(spectrogram):
    """
    beat spectrum 

    In: spectrogram[number of frequency bins][number of time bins]
    out: beat_spectrum[number of lag indices]
    """
    beatspec = _acorr(spectrogram.T)
    beatspec = np.mean(beatspec, axis=1) #make sure the shape of input spetrogram is correct

    return beatspec

def _periods(beatspec, period_range):
    """
    Compute the repeating period(s) from the beat spectrogram given a period range.
    Input:
        beatspec: beat spectrogram (or spectrum) (number_lags, n_timebin) (or (number_lags, ))
    Output:
        repeating_periods: repeating period(s) in lags (number_periods,) (or scalar)
    """
    repeating_periods = ( np.argmax( beatspec[ period_range[0] : min( period_range[1], int(np.floor(beatspec.shape[0] / 3) ) ) ] ) + 1 )

    # Re-adjust the indices to account for period range
    repeating_periods = repeating_periods + period_range[0]

    return repeating_periods

def _mask(spectrogram, repeating_period, power):

    """
    Compute the repeating mask for REPET.
    Inputs:
        spectrogram: audio spectrogram (number_frequencies, number_times)
        repeating_period: repeating period in lag
    Output:
        repeating_mask: repeating mask (number_frequencies, number_times)
    """
    # Get the number of frequency channels and time frames in the spectrogram
    n_freq, n_timebin = np.shape(spectrogram)

    # Estimate the number of segments (including the last partial one)
    n_segments = int(np.ceil(n_timebin / repeating_period))

    # Pad the end of the spectrogram to have a full last segment
    spectrogram = np.pad( spectrogram, ((0, 0), (0, n_segments * repeating_period - n_timebin)))

    # Reshape the padded spectrogram to a tensor of size (n_freq, n_timebin, n_segments)
    spectrogram = np.reshape( spectrogram, (n_freq, repeating_period, n_segments), order="F")

    # Compute the repeating mask
    repeating_segment = np.mean(spectrogram[:, :, :n_segments - 1],axis=2)

    # Derive the repeating spectrogram by ensuring it has less energy than the original spectrogram
    repeating_spectrogram = np.minimum( spectrogram, repeating_segment[:, :, np.newaxis] )

    # Derive the repeating mask by normalizing the repeating spectrogram by the original spectrogram
    # the fp eps was added in the author's code seemingly to prevent "invalid value encountered in true_divide"
    repeating_mask = (repeating_spectrogram + np.finfo(float).eps) / ( spectrogram + np.finfo(float).eps )

    # Reshape the repeating mask into (n_freq, n_timebin) and truncate to the original number of time frames
    repeating_mask = np.reshape( repeating_mask, (n_freq, n_segments * repeating_period), order="F" )
    repeating_mask = repeating_mask[:, 0: n_timebin]

    return repeating_mask