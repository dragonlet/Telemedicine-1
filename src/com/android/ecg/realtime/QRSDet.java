package com.android.ecg.realtime;

import android.util.Log;

// QRSDet.java
//
// Adapted from QRSdet2 by Patrick Hamilton www.physionet.org,
// which is based on Hamilton, Tomkins algorithm with changes for EC13
//
// QRSDetector implements a modified version of the QRS detection
// algorithm described in:
//
// Hamilton, Tompkins, W. J., "Quantitative investigation of QRS
// detection rules using the MIT/BIH arrhythmia database",
// IEEE Trans. Biomed. Eng., BME-33, pp. 1158-1165, 1987.

public class QRSDet {

    static final int SAMPLE_RATE = 300; // Sample rate in Hz
    private static final int MS10 = ((int) (10 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS25 = ((int) (25 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS30 = ((int) (30 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS80 = ((int) (80 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS95 = ((int) (95 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS100 = ((int) (100 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS125 = ((int) (125 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS150 = ((int) (150 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS160 = ((int) (160 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS175 = ((int) (175 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS195 = ((int) (195 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS200 = ((int) (200 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS220 = ((int) (220 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS250 = ((int) (250 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS300 = ((int) (300 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS360 = ((int) (360 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS450 = ((int) (450 / ((double) 1000 / (double) SAMPLE_RATE) + 0.5));
    private static final int MS1000 = SAMPLE_RATE;
    private static final int MS1500 = ((int) (1500 / ((double) 1000 / (double) SAMPLE_RATE)));
    private static final int DERIV_LENGTH = MS10;
    private static final int LPBUFFER_LGTH = ((int) (2 * MS25));
    private static final int HPBUFFER_LGTH = MS125;
    private static final int PRE_BLANK = MS195;
    private static final int MIN_PEAK_AMP = 7; // Prevents detections of peaks smaller than 150 uV.
    /// Detector threshold  = 0.3125 = TH_NUMERATOR/TH_DENOMINATOR
    private static final int TH_NUMERATOR = 3125;
    private static final int TH_DENOMINATOR = 10000;
    private static final int WINDOW_WIDTH = MS80;           // Moving window integration width.
    private static final int FILTER_DELAY = (int) (((double) DERIV_LENGTH / 2) + ((double) LPBUFFER_LGTH / 2 - 1) + (((double) HPBUFFER_LGTH - 1) / 2) + PRE_BLANK);  // filter delays plus 200 ms blanking delay
    private static final int DER_DELAY = WINDOW_WIDTH + FILTER_DELAY + MS100;    // Variables for peakHeight function
    private int m_peakMax = 0;
    private int m_peakTimeSinceMax = 0;
    private int m_peakLastDatum = 0;
    private int m_ddBuffer[] = new int[DER_DELAY]; // Buffer holding derivative data.
    private int m_ddPtr;
    private int m_delay = 0;
    private int m_detThresh,  m_qpkcnt = 0;
    private int m_qrsBuf[] = new int[8];
    private int m_noiseBuf[] = new int[8];
    private int m_rrBuf[] = new int[8];
    private int m_rsetBuf[] = new int[8];
    private int m_rsetCount = 0;
    private int m_nmean,  m_qmean,  m_rrmean;
    private int m_count;
    private int m_sbPeak = 0;
    private int m_sbLoc;
    private int m_sbCount = MS1500;
    private int m_maxder,  m_lastMax;
    private int m_initBlank,  m_initMax;
    private int m_preBlankCnt,  m_tempPeak;
    private QRSFilter m_qrsFilter = new QRSFilter();
    private Deriv m_deriv1 = new Deriv();

    public QRSDet() {
        init();
    }

    public void init() {
        // Initialize all buffers
        for (int i = 0; i < 8; ++i) {
            m_noiseBuf[i] = 0; // Initialize noise buffer
            m_rrBuf[i] = MS1000; // and R-to-R interval buffer.
        }

        m_qpkcnt = m_maxder = m_lastMax = m_count = m_sbPeak = 0;
        m_initBlank = m_initMax = m_preBlankCnt = m_ddPtr = 0;
        m_sbCount = MS1500;
        m_qrsFilter.init(); // initialise filters
        m_deriv1.init();
        peakHeight(0, 1);
        addSample(0);
    }

    public int addSample(int datum) {
        int fdatum;
        int QrsDelay = 0;
        int i, newPeak, aPeak;
        fdatum = m_qrsFilter.addSample(datum);   // Filter data.


        // Wait until normal detector is ready before calling early detections.
        aPeak = peakHeight(fdatum, 0);
        if (aPeak < MIN_PEAK_AMP) {
            aPeak = 0;        
            // Hold any peak that is detected for 200 ms
            // in case a bigger one comes along.  There
            // can only be one QRS complex in any 200 ms window.
        }
        newPeak = 0;
        
        // If there has been no peak for 200 ms save this one and start counting.
        if (aPeak != 0 && m_preBlankCnt == 0) 
        {                                       
            m_tempPeak = aPeak;
            m_preBlankCnt = PRE_BLANK;           // MS200
        } 
        // If we have held onto a peak for 200 ms pass it on for evaluation.
        else if (aPeak == 0 && m_preBlankCnt != 0) 
        {                                       
            if (--m_preBlankCnt == 0) {
                newPeak = m_tempPeak;
            }
        } 
        // If we were holding a peak, but this ones bigger, save it and start
        // counting to 200 ms again.
        else if (aPeak != 0) 
        { 
            if (aPeak > m_tempPeak) 
            {
                m_tempPeak = aPeak;
                m_preBlankCnt = PRE_BLANK; // MS200
            } else if (--m_preBlankCnt == 0) {
                newPeak = m_tempPeak;
            }
        }

        // Save derivative of raw signal for T-wave and baseline
        // shift discrimination.
        m_ddBuffer[m_ddPtr] = m_deriv1.addSample(datum);
        if (++m_ddPtr == DER_DELAY) {
            m_ddPtr = 0;
        }
        // Initialize the qrs peak buffer with the first eight
        // local maximum peaks detected. 
        if (m_qpkcnt < 8) {
            ++m_count;
            if (newPeak > 0) {
                m_count = WINDOW_WIDTH;
            }
            if (++m_initBlank == MS1000) {
                m_initBlank = 0;
                m_qrsBuf[m_qpkcnt] = m_initMax;
                m_initMax = 0;
                ++m_qpkcnt;
               
                if(m_qpkcnt == 8)
                {
                    m_qmean = mean( m_qrsBuf, 8 ) ;
                    m_nmean = 0 ;
                    m_rrmean = MS1000 ;
                    m_sbCount = MS1500+MS150 ;
                    m_detThresh = thresh(m_qmean,m_nmean) ;
                }
            }
            if (newPeak > m_initMax) {
                m_initMax = newPeak;
            }
        } else // Else test for a qrs.
        {
            ++m_count;
            if (newPeak > 0) {


                // Check for maximum derivative and matching minima and maxima
                // for T-wave and baseline shift rejection.  Only consider this
                // peak if it doesn't seem to be a base line shift.

                if (baselineShiftCheck(m_ddBuffer, m_ddPtr) == 0) {


                    // Classify the beat as a QRS complex
                    // if the peak is larger than the detection threshold.

                    if (newPeak > m_detThresh) {
                        shiftArrayValues(m_qrsBuf);
                        m_qrsBuf[0] = newPeak;
                        m_qmean = mean(m_qrsBuf, 8);
                        m_detThresh = thresh(m_qmean, m_nmean);
                        shiftArrayValues(m_rrBuf);
                        m_rrBuf[0] = m_count - WINDOW_WIDTH;
                        m_rrmean = mean(m_rrBuf, 8);
                        m_sbCount = m_rrmean + (m_rrmean >> 1) + WINDOW_WIDTH;
                        m_count = WINDOW_WIDTH;

                        m_sbPeak = 0;

                        m_lastMax = m_maxder;
                        m_maxder = 0;
                        QrsDelay = WINDOW_WIDTH + FILTER_DELAY;
                        m_initBlank = m_initMax = m_rsetCount = 0;
                    } 
                    // If a peak isn't a QRS update noise buffer and estimate.
                    // Store the peak for possible search back.
                    else {
                        shiftArrayValues(m_noiseBuf);
                        m_noiseBuf[0] = newPeak;
                        m_nmean = mean(m_noiseBuf, 8);
                        m_detThresh = thresh(m_qmean, m_nmean);

                        // Don't include early peaks (which might be T-waves)
                        // in the search back process.  A T-wave can mask
                        // a small following QRS.

                        if ((newPeak > m_sbPeak) && ((m_count - WINDOW_WIDTH) >= MS360)) {
                            m_sbPeak = newPeak;
                            m_sbLoc = m_count - WINDOW_WIDTH;
                        }
                    }
                }
            }

            // Test for search back condition.  If a QRS is found in
            // search back update the QRS buffer and m_detThresh.
            if ((m_count > m_sbCount) && (m_sbPeak > (m_detThresh >> 1))) {
                shiftArrayValues(m_qrsBuf);
                m_qrsBuf[0] = m_sbPeak;
                m_qmean = mean(m_qrsBuf, 8);
                m_detThresh = thresh(m_qmean, m_nmean);
                shiftArrayValues(m_rrBuf);
                m_rrBuf[0] = m_sbLoc;
                m_rrmean = mean(m_rrBuf, 8);
                m_sbCount = m_rrmean + (m_rrmean >> 1) + WINDOW_WIDTH;
                QrsDelay = m_count = m_count - m_sbLoc;
                QrsDelay += FILTER_DELAY;
                m_sbPeak = 0;
                m_lastMax = m_maxder;
                m_maxder = 0;

                m_initBlank = m_initMax = m_rsetCount = 0;
            }
        }

        // In the background estimate threshold to replace adaptive threshold
        // if eight seconds elapses without a QRS detection.
        if (m_qpkcnt == 8) {
            if (++m_initBlank == MS1000) {
                m_initBlank = 0;
                m_rsetBuf[m_rsetCount] = m_initMax;
                m_initMax = 0;
                ++m_rsetCount;

                // Reset threshold if it has been 8 seconds without a detection.
                if (m_rsetCount == 8) {
                    for (i = 0; i < 8; ++i) {
                        m_qrsBuf[i] = m_rsetBuf[i];
                        m_noiseBuf[i] = 0;
                    }
                    m_qmean = mean(m_rsetBuf, 8);
                    m_nmean = 0;
                    m_rrmean = MS1000;
                    m_sbCount = MS1500 + MS150;
                    m_detThresh = thresh(m_qmean, m_nmean);
                    m_initBlank = m_initMax = m_rsetCount = 0;
                }
            }
            if (newPeak > m_initMax) {
                m_initMax = newPeak;
            }
            
        }
        Log.i("mark", String.valueOf(newPeak));
        return (QrsDelay);
    }

    // Shift array values to the right
    private void shiftArrayValues(int data[]) {
        int nLength = data.length;
        for (int i = nLength - 1; i > 0; i--) {
            data[i] = data[i - 1];
        }
    }

    /**************************************************************
     * peakHeight() takes a datum as input and returns a peak height
     * when the signal returns to half its peak height, or
     **************************************************************/
    private int peakHeight(int datum, int init) {
        int pk = 0;

        if (init != 0) {
            m_peakMax = m_peakTimeSinceMax = 0;
        }
        if (m_peakTimeSinceMax > 0) {
            ++m_peakTimeSinceMax;
        }
        if ((datum > m_peakLastDatum) && (datum > m_peakMax)) {
            m_peakMax = datum;
            if (m_peakMax > 2) {
                m_peakTimeSinceMax = 1;
            }
        } else if (datum < (m_peakMax >> 1)) {
            pk = m_peakMax;
            m_peakMax = 0;
            m_peakTimeSinceMax = 0;
            m_delay = 0;
        } else if (m_peakTimeSinceMax > MS95) {
            pk = m_peakMax;
            m_peakMax = 0;
            m_peakTimeSinceMax = 0;
            m_delay = 3;
        }
        m_peakLastDatum = datum;
        return (pk);
    }

    /********************************************************************
    mean returns the mean of an array of integers.  It uses a slow
    sort algorithm, but these arrays are small, so it hardly matters.
     ********************************************************************/
    private int mean(int[] array, int datnum) {
        long sum;
        int i;

        for (i = 0    , sum = 0; i < datnum; ++i) {
            sum += array[i];
        }
        sum /= datnum;
        return ((int) sum);
    }

    /****************************************************************************
    thresh() calculates the detection threshold from the qrs mean and noise
    mean estimates.
     ****************************************************************************/
    private int thresh(int qmean, int nmean) {
        int thrsh;
        int dmed;

        dmed = qmean - nmean;
        dmed = (int) (dmed * TH_NUMERATOR / TH_DENOMINATOR);
        thrsh = nmean + dmed;
        return (thrsh);
    }

    /***********************************************************************
    baselineShiftCheck() reviews data to see if a baseline shift has occurred.
    This is done by looking for both positive and negative slopes of
    roughly the same magnitude in a 220 ms window.
     ***********************************************************************/
    private int baselineShiftCheck(int[] dBuf, int dbPtr) {
        int max, min, maxt, mint, t, x;
        max = min = maxt = mint = 0;

        for (t = 0; t < MS220; ++t) {
            x = dBuf[dbPtr];
            if (x > max) {
                maxt = t;
                max = x;
            } else if (x < min) {
                mint = t;
                min = x;
            }
            if (++dbPtr == DER_DELAY) {
                dbPtr = 0;
            }
        }

        m_maxder = max;
        min = -min;

        /* Possible beat if a maximum and minimum pair are found
        where the interval between them is less than 150 ms. */

        if ((max > (min >> 3)) && (min > (max >> 3)) && (Math.abs(maxt - mint) < MS150)) {
            return (0);
        } else {
            return (1);
        }
    }

    /*****************************************************************************
     *  Deriv implement derivative approximation represented by
     *  the difference equation:
     *
     *   y[n] = x[n] - x[n - 10ms]
     *
     *  Filter delay is DERIV_LENGTH/2
     *****************************************************************************/
    private class Deriv {

        int derBuff[] = new int[DERIV_LENGTH];
        int derI = 0;

        public Deriv() {
        }

        public void init() {
            for (derI = 0; derI < DERIV_LENGTH; ++derI) {
                derBuff[derI] = 0;
            }
            derI = 0;
        }

        public int addSample(int x) {
            int y;

            y = x - derBuff[derI];
            derBuff[derI] = x;
            if (++derI == DERIV_LENGTH) {
                derI = 0;
            }
            return (y);
        }
    }

    /******************************************************************************
     * Syntax:
     *   int QRSFilter(int datum, int init) ;
     * Description:
     *   QRSFilter() takes samples of an ECG signal as input and returns a sample of
     *   a signal that is an estimate of the local energy in the QRS bandwidth.  In
     *   other words, the signal has a lump in it whenever a QRS complex, or QRS
     *   complex like artifact occurs.  The filters were originally designed for data
     *  sampled at 200 samples per second, but they work nearly as well at sample
     *   frequencies from 150 to 250 samples per second.
     *
     *   The filter buffers and static variables are reset if a value other than
     *   0 is passed to QRSFilter through init.
     *******************************************************************************/
    private class QRSFilter {

        LPFilter m_lpFilter = new LPFilter();
        HPFilter m_hpFilter = new HPFilter();
        MWIntegrator m_mwIntegrator = new MWIntegrator();
        Deriv m_deriv = new Deriv();

        public QRSFilter() {
        }

        public void init() {
            m_lpFilter.init();      // Initialize filters.
            m_hpFilter.init();
            m_mwIntegrator.init();
            m_deriv.init();
            addSample(0);

        }

        public int addSample(int datum) {
            int fdatum;
            fdatum = m_lpFilter.addSample(datum);       // Low pass filter data.
            fdatum = m_hpFilter.addSample(fdatum);      // High pass filter data.
            fdatum = m_deriv.addSample(fdatum);         // Take the derivative.
            fdatum = Math.abs(fdatum);                 // Take the absolute value.
            fdatum = m_mwIntegrator.addSample(fdatum);  // Average over an 80 ms window .
            return (fdatum);
        }

        /*************************************************************************
         *  lpfilt() implements the digital filter represented by the difference
         *  equation:
         *
         *   y[n] = 2*y[n-1] - y[n-2] + x[n] - 2*x[t-24 ms] + x[t-48 ms]
         *
         *   Note that the filter delay is (LPBUFFER_LGTH/2)-1
         *
         **************************************************************************/
        private class LPFilter {

            long m_y1 = 0;
            long m_y2 = 0;
            int m_data[] = new int[LPBUFFER_LGTH];
            int m_ptr = 0;

            public LPFilter() {
            }

            public void init() {
                for (m_ptr = 0; m_ptr < LPBUFFER_LGTH; ++m_ptr) {
                    m_data[m_ptr] = 0;
                }
                m_y1 = m_y2 = 0;
                m_ptr = 0;
                addSample(0);
            }

            public int addSample(int datum) {
                long y0;
                int output;
                int halfPtr;
                
                halfPtr = m_ptr - (LPBUFFER_LGTH / 2); // Use halfPtr to index
                if (halfPtr < 0) // to x[n-6].
                {
                    halfPtr += LPBUFFER_LGTH;
                }
                y0 = (m_y1 << 1) - m_y2 + datum - (m_data[halfPtr] << 1) + m_data[m_ptr];
                m_y2 = m_y1;
                m_y1 = y0;
                output = (int) (y0 / ((LPBUFFER_LGTH * LPBUFFER_LGTH) / 4));
                m_data[m_ptr] = datum;           // Stick most recent sample into
                if (++m_ptr == LPBUFFER_LGTH) // the circular buffer and update
                {
                    m_ptr = 0;                 // the buffer pointer.
                }
                return (output);
            }
        }

        /******************************************************************************
         *  hpfilt() implements the high pass filter represented by the following
         *  difference equation:
         *
         *   y[n] = y[n-1] + x[n] - x[n-128 ms]
         *   z[n] = x[n-64 ms] - y[n] ;
         *
         *  Filter delay is (HPBUFFER_LGTH-1)/2
         ******************************************************************************/
        private class HPFilter {

            long m_y = 0;
            int m_data[] = new int[HPBUFFER_LGTH];
            int m_ptr = 0;

            public HPFilter() {
            }

            public void init() {
                for (m_ptr = 0; m_ptr < HPBUFFER_LGTH; ++m_ptr) {
                    m_data[m_ptr] = 0;
                }
                m_ptr = 0;
                m_y = 0;
                addSample(0);
            }

            public int addSample(int datum) {
                int z;
                int halfPtr;
                m_y += datum - m_data[m_ptr];
                halfPtr = m_ptr - (HPBUFFER_LGTH / 2);
                if (halfPtr < 0) {
                    halfPtr += HPBUFFER_LGTH;
                }
                z = m_data[halfPtr] - (int) (m_y / HPBUFFER_LGTH);

                m_data[m_ptr] = datum;
                if (++m_ptr == HPBUFFER_LGTH) {
                    m_ptr = 0;
                }

                return (z);
            }
        }


        // MWIntegrator implements a moving window integrator.  It averages
        // the signal values over the last WINDOW_WIDTH samples.
        private class MWIntegrator {

            long m_sum = 0;
            int m_data[] = new int[WINDOW_WIDTH];
            int m_ptr = 0;

            public MWIntegrator() {
            }

            public void init() {
                for (m_ptr = 0; m_ptr < WINDOW_WIDTH; ++m_ptr) {
                    m_data[m_ptr] = 0;
                }
                m_sum = 0;
                m_ptr = 0;
                addSample(0);
            }

            public int addSample(int datum) {
                int output;

                m_sum += datum;
                m_sum -= m_data[m_ptr];
                m_data[m_ptr] = datum;
                if (++m_ptr == WINDOW_WIDTH) {
                    m_ptr = 0;
                }
                if ((m_sum / WINDOW_WIDTH) > 32000) {
                    output = 32000;
                } else {
                    output = (int) (m_sum / WINDOW_WIDTH);
                }
                return (output);
            }
        }
    }
}

