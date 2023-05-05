#include <math.h>
#include <algorithm> //sort is in here
#include <vector>
#include "kiss_fft/kiss_fft.c"
#include "AudioFile.h"
#include <stdint.h>
#include <jni.h>

inline int _argmax(float *array, int minIdx, int maxIdx);
inline float _HammWindow(int win_size, int i);
inline void repet(std::vector<float> &signal, std::vector<float> &background_signal,int samples, int Fs, int min_period, int max_period);

std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

bool replace(std::string& str, const std::string& from, const std::string& to) {
    size_t start_pos = str.find(from);
    if(start_pos == std::string::npos)
        return false;
    str.replace(start_pos, from.length(), to);
    return true;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_voicerecorder_FragmentAudioRecorder_cpprepet(JNIEnv *env, jclass clazz,
                                                              jstring folderpath,
                                                              jstring filename) {

    std::string folder_path = jstring2string(env, folderpath);
    std::string file_name = jstring2string(env, filename);

    AudioFile<long long> audioFile;

//load file
    audioFile.load(folder_path + "/" + file_name);
    uint32_t Fs = audioFile.getSampleRate();
    int numSamples = audioFile.getNumSamplesPerChannel();
    std::vector<float> input(numSamples);
    int min_period = 1;
    int max_period = 10; //period range allowed, seconds

    for (int i = 0; i < numSamples; i++) {
        input[i] = (float)(audioFile.samples[0][i]);
    }

    std::vector<float> BG(numSamples);
    repet(input, BG, numSamples, Fs, min_period, max_period);

    for (int i = 0; i < numSamples; i++) {
        audioFile.samples[0][i] = (long long)(input[i] - (BG[i]));
    }

    replace(file_name, ".wav", "_melody.wav");

    audioFile.save(folder_path + "/" + file_name, AudioFileFormat::Wave);

    replace(file_name, "_melody.wav", "_background.wav");

    for (int i = 0; i < numSamples; i++) {
        audioFile.samples[0][i] = (long long)(BG[i]);
    }

    audioFile.save(folder_path + "/" + file_name,AudioFileFormat::Wave);
}

//get hamming window of win_size length
inline float _HammWindow(int win_size, int i) {
    const float LIBROSA_PI = 3.14159265358979323846;
    return (0.54f - 0.46f * cosf(2 * LIBROSA_PI * (float)i / (float)(win_size-1)));
}

//assume a 1-channel audio sample here, full REPET calculation
//pass in sample rate, store read signal in an array called signal
//signal, background are 1D vectors of format float, samples in number, Fs in Hz, periods in seconds that will be converted automatically
inline void repet(std::vector<float> &signal, std::vector<float> &background_signal,  int samples, int Fs, int min_period, int max_period){
    //stage 1
    //using 40ms chunks of data for STFT, window size should be 2 to the nearest power to achieve that time, 50% over
    int winsize = pow(2,ceil(log2(Fs * 0.04))); //size of 50% overlap window
    int overlap = winsize/2; //size of stft overlap
    int nfreq = (int)(winsize/2+1); //number of freqs in spectrogram
    int tframe = int(ceil((float)samples/(float)overlap) + 1);//n of time frames total

    std::vector<std::vector<kiss_fft_cpx>> stft(winsize, std::vector<kiss_fft_cpx> (tframe) );
    std::vector<std::vector<float>> specgram(nfreq, std::vector<float> (tframe,0));

    //stft wit 50% overlap implementation
    //fftpoint is the # of timeframe currently on. # of starting index is thus fftpoint * overlap
    for (int fftpoint=0; fftpoint<tframe; fftpoint++) {
        kiss_fft_cfg cfg = kiss_fft_alloc(winsize,false,0,0);
        kiss_fft_cpx cx_in[winsize];
        kiss_fft_cpx cx_out[winsize];
        // populate cx_in with windowed signal, account for incomplete window
        for (int i=0; i<winsize; i++){
            if ((fftpoint*overlap+i)<samples) {
                float win = _HammWindow(winsize, i);
                cx_in[i].r = signal[(int) (fftpoint * overlap + i)] * win;
            } else {
                cx_in[i].r = 0;
            }
            cx_in[i].i = 0;
            cx_out[i].r = 0;
            cx_out[i].i = 0;
        }
        // perform fft of window section
        kiss_fft(cfg, cx_in, cx_out);
        free(cfg);

        //pass data into cx_out, normalize by winsize
        for (int i=0; i<winsize; i++) {
            stft[i][fftpoint].r = cx_out[i].r/(float)winsize;
            stft[i][fftpoint].i = cx_out[i].i/(float)winsize;
        }
    }

    //square the positive part of the spectrogram to use as power spectrogram
    for (int i = 0; i<nfreq; i++) {
        for (int j = 0; j < tframe; j++) {
            specgram[i][j] = pow(stft[i][j].r,2) + pow(stft[i][j].i,2);
        }
    }

    //calculate beat spectrum, adjust period range unit from seconds to indices
    float beat_spectrum[tframe];
    std::vector<std::vector<float>> autocorr(nfreq, std::vector<float> (tframe,0));

    //calculate autocorrelation of each row, loop through frequency, using the weiner theorem
    for (int r=0; r<nfreq; r++) {
        kiss_fft_cfg cfg = kiss_fft_alloc(tframe,0,0,0);
        kiss_fft_cpx cx_in[tframe];
        kiss_fft_cpx cx_out[tframe];
        //put ith sample into in.r
        for (int i=0; i<tframe; i++) {
            cx_in[i].r = specgram[r][i];
            cx_in[i].i = 0;
            cx_out[i].r = 0;
            cx_out[i].i = 0;
        }
        //fft
        kiss_fft(cfg, cx_in, cx_out);
        //manually do complex multiplication, real and imaginary get squared, complex part disappear due to conjugate
        for (int k=0; k<tframe; k++){
            cx_out[k].r = pow(cx_out[k].r/float(tframe),2) + pow(cx_out[k].i/float(tframe),2);
            cx_out[k].i = 0;
        }
        //inverse cx_out back to cx_in
        kiss_fft_cfg icfg = kiss_fft_alloc(tframe,1,0,0);
        kiss_fft(icfg, cx_out, cx_in);
        //autocorrelation is cx_in
        for (int k=0; k<tframe; k++){
            autocorr[r][k] = cx_in[k].r;
        }

        free(cfg);
        free(icfg);
    }

    //calculate the beat spectrum as the mean of the autocorrelation on each frequency
    //used for finding the repeating period
    for (int i=0; i<tframe; i++){
        for (int j=0; j<nfreq; j++)
            beat_spectrum[i] += autocorr[j][i];
        beat_spectrum[i]/=(float)nfreq;
    }

    //find the repeating period from beat spectrum
    //adjust the min/max indices using this formula below
    //newperiod = np.round(np.array(period) *  Fs / overlap)
    int min_IDX = round(min_period * Fs / overlap);
    int max_IDX = round(max_period * Fs / overlap);
    max_IDX = std::min( max_IDX, (int)floor(nfreq/3) );
    int repeating_period = _argmax(beat_spectrum, min_IDX, max_IDX)+1;

    //stage 2 and 3
    //calculate and apply repeating frequency map
    int n_segment = (int)ceil(tframe/repeating_period);

    std::vector<std::vector<float>> repeating_mask(winsize, std::vector<float> (tframe,0)); //the repeating mask calculated by normalizing repeating stft to original stft

    std::vector<std::vector<float>> repeating_pattern(nfreq, std::vector<float> (repeating_period,0));//repeating pattern of length repeating_period

    for(int i=0; i<nfreq; i++){

        for(int l=0; l<repeating_period;l++){

            /*
            int n = 0;
            for (int k=0;k<n_segment;k++) {
                int j = l + (k) * repeating_period;
                if (j < tframe) {
                    repeating_pattern[i][l] += specgram[i][j];
                    n++;
                }
            }
            repeating_pattern[i][l]/=n;
           */
            std::vector<float> segs; //dummy array for median
            int n = 0;

            //fill in repeating pattern by calculating median along the n_segment
            //does not account for the final segment that has zero-pads

            for (int k=0;k<n_segment;k++) {
                int j = l+(k)*repeating_period;
                if (j < tframe) {
                    segs.push_back(specgram[i][j]);
                    n++;
                }
            }

            if (n % 2 == 0) {
                // Applying nth_element
                // on n/2th index
                nth_element(segs.begin(),
                            segs.begin() + n / 2,
                            segs.end());
                // Applying nth_element
                // on (n-1)/2 th index
                nth_element(segs.begin(),
                            segs.begin() + (n - 1) / 2,
                            segs.end());

                // Find the average of value at
                // index N/2 and (N-1)/2
                repeating_pattern[i][l] = (segs[(n - 1)]/ 2 + segs[n / 2] ) / 2.0f;
            }
                // If size of the arr[] is odd
            else {
                // Applying nth_element
                // on n/2
                nth_element(segs.begin(),
                            segs.begin() + n / 2,
                            segs.end());
                repeating_pattern[i][l] = segs[n / 2];
            }

            //find the repeating spectrogram by comparing repeating_pattern and original stft
            for(int k=0;k<n_segment;k++) {
                int j = l+(k)*repeating_period;
                if (j<tframe) {
                    repeating_mask[i][j] = std::min(
                            specgram[i][j],  repeating_pattern[i][l]
                    );

                    repeating_mask[i][j]=  (repeating_mask[i][j] + std::numeric_limits<float>::epsilon() ) /
                                               (specgram[i][j] + std::numeric_limits<float>::epsilon());

                }

            }

        }

    }

    //symmetrize repeating_mask to apply to stft, see np.concatenate(mask, mask[-2,0,-1], axis=0)
    for(int i=0; i<nfreq; i++){
        for(int j=0; j<tframe; j++){
            repeating_mask[winsize-i-1][j] = repeating_mask[i][j];
        }
    }

    //apply repeating frequency map to stft
    for (int i=0; i<winsize; i++) {
        for (int j=0; j<tframe; j++) {
            stft[i][j].r *= repeating_mask[i][j];
            stft[i][j].i *= repeating_mask[i][j];
        }
    }

    //istft with 50% overlap and window?
    for (int i=0; i<tframe; i++) {
        kiss_fft_cfg icfg = kiss_fft_alloc(winsize,1,0,0);
        kiss_fft_cpx cx_in[winsize];
        kiss_fft_cpx cx_out[winsize];

        // populate cx_in with each time frame data
        for (int j=0; j<winsize; j++){
            cx_in[j] = stft[j][i];
            cx_out[i].r = 0;
            cx_out[i].i = 0;
        }

        kiss_fft(icfg, cx_in, cx_out);
        free(icfg);

        for (int j=0; j<winsize; j++) {
            int fftpoint = (int)(i*overlap+j);
            //put data back into background_signal account for trailing zeros
            if (fftpoint < samples) {
                background_signal[fftpoint] += cx_out[j].r * _HammWindow(winsize, j);
            }
        }

    }

}

//max index in array
inline int _argmax(float *array, int minIdx, int maxIdx) {
    int ret_idx = minIdx;

    for (int i = minIdx; i < maxIdx; i++) {
        if (array[i] > array[ret_idx]) {
            ret_idx = i;
        }
    }

    return ret_idx;
}
