package com.android.ecg.realtime;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultText;

public class Detector {
	/*NSRDector
	 * �?���?
	 * */
    private Wavelets _Wavelets;
    private VE _VE;
    private Peaks _Peaks;      
    private NEWFMCore _NEWFMCore;

    private double[] _dNEWFMInput;
    private float[] _fData;
    private int _iDataIndex;
    
    private  final int WLEVEL_D3=0;
    private  final int WLEVEL_D4=1;
    
    
    public Detector() {
		// TODO Auto-generated constructor stub
	}

	public void Add(Vector<Float> testVector) {
//    	File file=new File(path);
//    	try {
//			BufferedReader br=new BufferedReader(new FileReader(file));
//			String line=br.readLine();
//			for (int i=0;line!=null&&i<_fData.length;i++) {
//				_fData[i]=-Float.parseFloat(line);
//				line=br.readLine();
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		for (int i = 0; i < testVector.size(); i++) {
			_fData[i]= testVector.get(i);
		}
    	
	}
    /*XML Element*/   //HBX       <A0>    Value
    private Hashtable<String, Map<String, Vector<Double>>> hashtable
    		=new Hashtable<String, Map<String, Vector<Double>>>();
	 private SAXReader reader;
    
    private float Average(float[] array){
    	float sum = 0;
    	for (int i = 0; i < array.length; i++) {
			sum+=array[i];
		}
    	return sum/array.length;
    }
    
    private double[][] ArrayClear(double[][] array) {
		array=new double[array.length][array[0].length];
		return array;
	}
    
    private float[] ArrayClear(float[] array) {
		array=new float[array.length];
		return array;
	}
    
    private float Max(float[] array) {
    	float[] tempArray=array.clone();
		Arrays.sort(tempArray);
		return tempArray[array.length-1];
	}
    
    private float Min(float[] array) {
    	float[] tempArray=array.clone();
		Arrays.sort(tempArray);
		return tempArray[0];
	}
    
	public class Wavelets
    {
        private final int D_THREE = 256;
        private final int D_FOUR = 128;

        private final double ROOT_2 = 1.414214;
        private final int WIN_POINT = 48;

        private int _iLevel;
        private int _iWindowSize;
        private float[] _fData;
        private float[] _fResult;
        private float[] _fTemp;
        private float[][] _fMother;
        private float[] _fD3;
        private float[] _fD4;
        private String _sflog;
//↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑위는 Wavelets �?��

        
         
        
        public Wavelets(int iLevel, int iWindowSize, int iMother)
        {
            _iLevel = iLevel;
            _iWindowSize = iWindowSize;//2000
            _fData = new float[iWindowSize + WIN_POINT];
            _fResult = new float[iWindowSize + WIN_POINT];
            _fTemp = new float[(iWindowSize + WIN_POINT) / 2];
            _fMother = new float[2][];
            _fMother[0] = new float[2];
            _fMother[1] = new float[2];
            _fD3 = new float[D_THREE];
            _fD4 = new float[D_FOUR];
            _sflog = " ";//0 is nsr , 1 is vf

            SetWavelets(iMother);
        }

        Wavelets()
        {

        }

        private void SetWavelets(int iMother)
        {
            if (iMother == 11)
            {
                _fMother[0][0] = (float)(1.0 / ROOT_2);
                _fMother[0][1] = (float)(1.0 / ROOT_2);
                _fMother[1][0] = (float)(1.0 / ROOT_2);
                _fMother[1][1] = (float)(-1.0 / ROOT_2);
            }
        }


        public void Process(float[] fData)
        {
            int iWinSize = (_iWindowSize + WIN_POINT) / 2;

            System.arraycopy(fData, 0,_fData, 0,fData.length);
//            _fData=fData.clone();
            
            for (int j = 0, k = 0; j < iWinSize; ++j, k += 2)
            {
                _fTemp[j] = _fMother[0][0] * _fData[k];
                _fTemp[j] += _fMother[0][1] * _fData[k + 1];

                _fResult[j + iWinSize] = _fMother[1][0] * _fData[k];
                _fResult[j + iWinSize] += _fMother[1][1] * _fData[k + 1];
            }

            for (int i = 1, l = 4; i < _iLevel; ++i, l *= 2)
            {
                //	2048 / 4	= 512
                //	2048 / 8	= 256
                //	2048 / 16	= 128
                iWinSize = (_iWindowSize + WIN_POINT) / l;

                for (int j = 0, k = 0; j < iWinSize; ++j, k += 2)
                {
                    _fResult[j + iWinSize] = _fMother[1][0] * _fTemp[k]; // Detail,
                    _fResult[j + iWinSize] += _fMother[1][1] * _fTemp[k + 1];

                    _fTemp[j] = _fMother[0][0] * _fTemp[k] + _fMother[0][1] * _fTemp[k + 1];
                }
            }

            // 앞의 1024개의 데이터를 채워 넣음
            // Approximation -> Result
            for (int i = 0; i < iWinSize; ++i)
            {
                _fResult[i] = _fTemp[i];
            }
        }

        public float[] GetDataD3()
        {
            for (int i = 0; i < D_THREE; ++i)
            {
                _fD3[i] = _fResult[i + D_THREE];
            }

            return _fD3;
        }

        public float[] GetDataD4()
        {
            for (int i = 0; i < D_FOUR; i++)
            {
                _fD4[i] = _fResult[i + D_FOUR];
            }

            return _fD4;
        }
        //----------2010-01-02-add
        public String PreDetecorVF(float[] fData)
        {   
            return _sflog="";//
        }

        public void BubbleSort(float[] list)
        {
            for (int i = 0; i < list.length; i++)
            {
                for (int j = i; j < list.length; j++)
                {
                    if (list[i] < list[j])
                    {
                        float temp = list[i];
                        list[i] = list[j];
                        list[j] = temp;
                    }
                }
            }
        }
    }

	
	class Peaks
    {
        private final int D_THREE = 256;
        private final int D_FOUR = 128;
        private final int PEAKAB = 3;

        private float[] _fLevel3;
        private float[] _fLevel4;
        private float[] _fDataNSR;

        public float[] _fResult_D3;
        public float[] _fResult_D4;
        public float[] _fPeak_TeamD3;
        public float[] _fPeak_TeamD4;
        public float[][] _fPeak_Team_2D3;
        public float[][] _fPeak_Team_2D4;
        public int _iPeaks;

        public Peaks()
        {
            _fLevel3 = new float[D_THREE];
            _fLevel4 = new float[D_FOUR];

            _fResult_D3 = new float[8];
            _fResult_D4 = new float[4];

            _fPeak_TeamD3 = new float[D_THREE];
            _fPeak_TeamD4 = new float[D_FOUR];
            _fPeak_Team_2D3 = new float[D_THREE][2];
            _fPeak_Team_2D4 = new float[D_FOUR][2];

            _fDataNSR = new float[250];

            _iPeaks = 0;
        }

        

        private int CountPeaksD3(float[] tmp)
        {
            float fFirst_Av = 0;
            int iCountPeak = 0;
            float fPeak_Team_Av = 0;
            float fPeak_Team_Sum = 0;

            for (int i = 0; i < D_THREE; ++i)
            {
                _fLevel3[i] = tmp[i];
            }
            
            fFirst_Av = Average(tmp);
            _fPeak_TeamD3=new float[_fPeak_TeamD3.length];
//            Array.Clear(_fPeak_TeamD3, 0, _fPeak_TeamD3.length);

            for (int i = 0; i < D_THREE - PEAKAB; ++i)
            {
                if ((tmp[i] < tmp[i + 1]) &
                    (tmp[i + 2] < tmp[i + 1]) &
                    (fFirst_Av < tmp[i + 1]))// &
                //			(tmp[i + 1] > 0.00001)) 
                {
                    _fPeak_TeamD3[i + 1] = tmp[i + 1]; // error 있다. 09-05-08
                    iCountPeak = iCountPeak + 1;
                }
            }

            for (int i = 0; i < D_THREE; i++)
            {
                fPeak_Team_Sum = fPeak_Team_Sum + _fPeak_TeamD3[i];
            }

            fPeak_Team_Av = fPeak_Team_Sum / iCountPeak;


            int k = 0;
            for (int i = 0; i < D_THREE; i++)
            {
                if (fPeak_Team_Av < _fPeak_TeamD3[i])
                {
                    _fPeak_Team_2D3[k][0] = _fPeak_TeamD3[i];
                    _fPeak_Team_2D3[k][1] = (float)i;
                    k++;
                }
            }
            //mfba 계산하기 위해�?sort �?pickUpMaxPeak 구현한다.
            //sort(fpeak_team_2D3, k, D_THREE);		
            //pickUpMaxPeak(fpeak_team_2D3, 3);	

            return _iPeaks = k;
        }

        private int CountPeaksD4(float[] tmp)
        {
            float fFirst_Av = 0;
            int iCountPeak = 0;
            float fPeak_Team_Av = 0;
            float fPeak_Team_Sum = 0;

            for (int i = 0; i < D_FOUR; ++i)
            {
                _fLevel4[i] = tmp[i];
            }
            
            fFirst_Av = Average(tmp);

            for (int i = 0; i < D_FOUR - PEAKAB; ++i)
            {
                if ((tmp[i] < tmp[i + 1]) &
                    (tmp[i + 2] < tmp[i + 1]) &
                    (fFirst_Av < tmp[i + 1]))
                {
                    _fPeak_TeamD4[i + 1] = tmp[i + 1];
                    iCountPeak = iCountPeak + 1;
                }
            }

            for (int i = 0; i < D_FOUR; i++)
            {
                fPeak_Team_Sum = fPeak_Team_Sum + _fPeak_TeamD4[i];
            }

            fPeak_Team_Av = fPeak_Team_Sum / iCountPeak;

            int k = 0;
            for (int i = 0; i < D_FOUR; i++)
            {
                if (fPeak_Team_Av < _fPeak_TeamD4[i])
                {
                    _fPeak_Team_2D4[k][0] = _fPeak_TeamD4[i];
                    _fPeak_Team_2D4[k][1] = (float)i;
                    k++;
                }
            }
            // 배열 3,0 �?�� 값이 차이 난다.
            Sort(_fPeak_Team_2D4, k);
            PickUpMaxPeak(_fPeak_Team_2D4, 4);

            return _iPeaks = k;
        }

        private void Sort(float[][] fPeak_Team, int iPeak_Count)
        {
            float[][] temp = new float[1][2];

            for (int i = 0; i < iPeak_Count; i++)
            {
                for (int j = 0; j < iPeak_Count; j++)
                {
                    if (fPeak_Team[j][0] < fPeak_Team[j + 1][ 0])
                    {
                        temp[0][0] = fPeak_Team[j][0];
                        temp[0][1] = fPeak_Team[j][1];
                        fPeak_Team[j][0] = fPeak_Team[j + 1][0];
                        fPeak_Team[j][1] = fPeak_Team[j + 1][1];
                        fPeak_Team[j + 1][0] = temp[0][0];
                        fPeak_Team[j + 1][1] = temp[0][1];
                    }
                }
            }
        }

        private void PickUpMaxPeak(float[][] fPeak_Max, int iLevel)
        {
            if (iLevel == 3)
            {
                int iMax_Index = 0;
                int iIndex_Front = 0;
                float[][] fMax_Front = new float[4][];
                float[][] fMax_Back = new float[4][];

                for (int i = 0; i < 3; i++)
                {
                    iMax_Index = (int)fPeak_Max[iIndex_Front][1];

                    if (iMax_Index == 0 || iMax_Index == 1 ||
                       iMax_Index == 2 || iMax_Index == 3)
                    {
                        iIndex_Front++;
                    }
                    else
                    {
                        for (int j = 0; j < 3; j++)
                        {
                            for (int k = 0; k < 4; k++)
                            {
                                fMax_Front[k][j] = _fLevel3[iMax_Index - (k + 1)];
                                fMax_Back[k][j] = _fLevel3[iMax_Index + k + 1];
                            }
                            iIndex_Front++;
                        }
                    }
                }
                for (int i = 0; i < 8; i++)
                {
                    if (i < 4)
                    {
                        _fResult_D3[i] = Average(fMax_Front[i]);
                    }
                    else
                    {
                        _fResult_D3[i] = Average(fMax_Back[i - 4]);
                    }
                }
            }
            if (iLevel == 4)
            {

                int iMax_Index = 0;
                int iIndex_Front = 0;   
             
                float[] fMax_Front_f5 = new float[3];
                float[] fMax_Front_f6 = new float[3];
                float[] fMax_Back_b5 = new float[3];
                float[] fMax_Back_b6 = new float[3];

                fMax_Front_f5=ArrayClear(fMax_Front_f5);
                fMax_Front_f6=ArrayClear(fMax_Front_f6);
                fMax_Back_b5=ArrayClear(fMax_Back_b5);
                fMax_Back_b6=ArrayClear(fMax_Back_b6);

                for (int i = 0; i < 3; i++)
                {
                    iMax_Index = (int)fPeak_Max[iIndex_Front][1];

                    if (iMax_Index == 0 || iMax_Index == 1 ||
                        iMax_Index == 2 || iMax_Index == 3)
                    {
                        iIndex_Front++;
                    }
                    else
                    {
                        fMax_Front_f5[i] = _fLevel4[iMax_Index - 1];
                        fMax_Front_f6[i] = _fLevel4[iMax_Index - 2];

                        fMax_Back_b5[i] = _fLevel4[iMax_Index + 1];
                        fMax_Back_b6[i] = _fLevel4[iMax_Index + 2];

                        iIndex_Front++;
                    }
                }

                _fResult_D4[0] = Average(fMax_Front_f5);
                _fResult_D4[1] = Average(fMax_Front_f6);

                _fResult_D4[2] = Average(fMax_Back_b5);
                _fResult_D4[3] = Average(fMax_Back_b6);
            }
        }

       
        
        public int CountPeaks(float[] tmp, int wLevel)
        {
            int iResult = 0;

            switch (wLevel)
            {
                case WLEVEL_D3:
                    iResult = CountPeaksD3(tmp);
                    break;

                case WLEVEL_D4:
                    iResult = CountPeaksD4(tmp);
                    break;
            }

            return iResult;
        }

        public float GetDataDF(int i)
        {
            return _fResult_D4[i];
        }

        public float GetDataDT(int i)
        {
            return _fResult_D3[i];
        }
    }
	
	
	
	public  boolean Init(String szParam,InputStream is)
    {
        String[] param = szParam.split(",");

        int iLevel = Integer.parseInt(param[0]);
        int iWinSize = Integer.parseInt(param[1]);
        int iMother = Integer.parseInt(param[2]);
        int iPhase = Integer.parseInt(param[3]);
        int iSample = Integer.parseInt(param[4]);

        int iCoor = Integer.parseInt(param[5]);

        _Wavelets = new Wavelets(iLevel, iWinSize, iMother);
        _VE = new VE(iPhase, iSample, iCoor);
        _Peaks = new Peaks();
        _NEWFMCore = new NEWFMCore();

        _dNEWFMInput = new double[5];//8-->5
        _fData = new float[iWinSize];
        _iDataIndex = 0;

        _NEWFMCore.LoadXML(is);//----  
        return true;
    }
	
	
	
	//Main detect process
	public  String Process()
    {
        float[] fD3, fD4;
        double dResult;
        
        _dNEWFMInput=new double[_dNEWFMInput.length];
        

        //----------2010-01-04 adding
        String predetectorvf = "NSR";//_Wavelets.PreDetecorVF(_fData);

        if (predetectorvf.equals("NSR"))
        {
            _Wavelets.Process(_fData);
            fD3 = _Wavelets.GetDataD3();
            fD4 = _Wavelets.GetDataD4();

            _dNEWFMInput[0] = _VE.Process(fD3);
            _dNEWFMInput[1] = (double)_Peaks.CountPeaks(fD3, WLEVEL_D3);
            double temp5 = (double)_Peaks.CountPeaks(fD4, WLEVEL_D4);
            _dNEWFMInput[2] = _Peaks.GetDataDF(1) + 12.5;
            _dNEWFMInput[3] = _Peaks.GetDataDF(3) + 12.5;
            /*_dNEWFMInput[2] = _Peaks.GetDataDT(3);
            _dNEWFMInput[3] = _Peaks.GetDataDF(1);
            _dNEWFMInput[4] = _Peaks.GetDataDT(6);
            _dNEWFMInput[5] = _Peaks.GetDataDT(7);
            _dNEWFMInput[6] = _Peaks.GetDataDF(3);
            */

            dResult = _NEWFMCore.OneTestNewFMGo(_dNEWFMInput);

            _NEWFMCore.Reset();
        
            return dResult == 1.0 ? "NSR" : "VF";
        }
        return predetectorvf;
    }
	 
	class NEWFMCore
	    {
	        private HyperBox _HyperBox;
	        private double[][] _dTest_Data;
	        private double[][] _dTest_Data1;
	        private double[][] _dResult_HB0;
	        private double[][] _dResult_HB1;
	        private double[][] _dSum_HB;

	        public NEWFMCore()
	        {
	            _HyperBox = new HyperBox();
	            _dTest_Data = new double[1][5];//8----5
	            _dTest_Data1 = new double[1][10];
	            _dResult_HB0 = new double[1][4];//7----4
	            _dResult_HB1 = new double[1][4];//7----4
	            _dSum_HB = new double[11130][4];//7----4

	            Reset();
	        }

//	        ~NEWFMCore()
//	        {
//	        }

	        public void LoadXML(InputStream is)
	        {
	         XMLTableReader xtr = new XMLTableReader(is);
//	         DataTable[] HBX = new DataTable[2];
//	         DataTable[] HBY = new DataTable[2];
	         
	         Hashtable<Integer, Map<String, Vector<Double>>> HBX=new Hashtable<Integer, Map<String, Vector<Double>>>();
	         Hashtable<Integer, Map<String, Vector<Double>>> HBY=new Hashtable<Integer, Map<String, Vector<Double>>>();
	         
	            int iTableCount;

	            xtr.LoadXML(is);
	            iTableCount = xtr.GetTableCount();

	            for (int i = 0; i < 2; i++)
	            {
	            	String hBX="HBX"+i;
	            	String hBY="HBY"+i;
	                HBX.put(i , hashtable.get(hBX));
	                HBY.put(i , hashtable.get(hBY)) ;
	            }

	            _HyperBox.CreateHyperBox(iTableCount / 2, 4, 5);//7-->4   x=2,y=4,z=5

//	            HBX[0] = xtr.GetTable("HBX0");
//	            HBX[1] = xtr.GetTable("HBX1");
//	            HBY[0] = xtr.GetTable("HBY0");
//	            HBY[1] = xtr.GetTable("HBY1");

	            for(int i = 0; i < 2; i++)
	            {
	                int j = 0;
	                for (int l=0;l<4;l++) //HBX[i].Rows:A0\A1\A2\A3
	                {
	                	String A="A"+l;
	                	
	                    for(int k = 0; k < 5; k++)//5--->2
	                    {
	                        _HyperBox._dHB_x[i][j][k] = 
	                            HBX.get(i).get(A).get(k);  //6-->3
	                    }
	                    j++;
	                }
	                
	                j = 0;
	                for(int m=0;m<4;m++)
	                {
	                	String A="A"+m;
	                    for (int k = 0; k < 5; k++)//5--->2
	                    {
	                        _HyperBox._dHB_y[i][j][k] =
	                            HBY.get(i).get(A).get(k);//6-->3
	                    }
	                    j++;
	                }
	            }
	            

	        }
	      
	        public double OneTestNewFMGo(double[] dFieldValue)
	        {
	            int iInput_Num = 1;
	            int iFeature_Num = 4;//7-->4

	            for (int j = 0; j < iFeature_Num; j++)
	            {
	                _dTest_Data[0][j] = dFieldValue[j];
	                _dTest_Data1[0][j] = dFieldValue[j];
	            }

	            double dTemp_A = 0;
	            double dTemp_B = 0;
	            long ind_A = 0;
	            long ind_B = 0;
	            long ind_C = 0;
	            long ind_D = 0;

	            for (int k = 0; k < iInput_Num; k++)
	            {
	                for (int a = 0; a < iFeature_Num; a++)
	                {
	                    for (int b = 0; b < 4; b++)
	                    {
	                        if ((_dTest_Data[k][a] > _HyperBox._dHB_x[0][a][b]) &
	                            (_dTest_Data[k][a] <= _HyperBox._dHB_x[0][a][b + 1]))
	                        {
	                            dTemp_A = (_HyperBox._dHB_y[0][a][b] - _HyperBox._dHB_y[0][a][b + 1]) /
	                                (_HyperBox._dHB_x[0][a][b] - _HyperBox._dHB_x[0][a][b + 1]);
	                            dTemp_B = _HyperBox._dHB_y[0][a][b] - dTemp_A * _HyperBox._dHB_x[0][a][b];
	                            _dResult_HB0[k][a] = dTemp_A * +_dTest_Data[k][a] + dTemp_B;
	                        }

	                        if ((_dTest_Data[k][a] > _HyperBox._dHB_x[1][a][b]) &
	                            (_dTest_Data[k][a] <= _HyperBox._dHB_x[1][a][b + 1]))
	                        {
	                            dTemp_A = (_HyperBox._dHB_y[1][a][b] - _HyperBox._dHB_y[1][a][b + 1]) /
	                                (_HyperBox._dHB_x[1][a][b] - _HyperBox._dHB_x[1][a][b + 1]);
	                            dTemp_B = _HyperBox._dHB_y[1][a][b] - dTemp_A * _HyperBox._dHB_x[1][a][b];
	                            _dResult_HB1[k][a] = dTemp_A * _dTest_Data[k][a] + dTemp_B;
	                        }
	                    }
	                }

	                for (int c = 0; c < iFeature_Num; c++)
	                {
	                    _dSum_HB[k][0] += _dResult_HB0[k][c];
	                    _dSum_HB[k][1] += _dResult_HB1[k][c];
	                }

	                if (_dSum_HB[k][0] >= _dSum_HB[k][1])
	                {
	                    if (_dTest_Data[k][iFeature_Num] == 1)
	                    {
	                        ind_A++;
	                        _dTest_Data[k][iFeature_Num + 1] = 1;
	                        _dTest_Data[k][iFeature_Num + 2] = 1;
	                    }
	                    else
	                    {
	                        ind_C++;
	                        _dTest_Data1[k][iFeature_Num + 1] = 1;
	                        _dTest_Data1[k][iFeature_Num + 2] = 0;
	                    }
	                }
	                else if (_dSum_HB[k][0] < _dSum_HB[k][1])
	                {
	                    if (_dTest_Data[k][iFeature_Num] == 2)
	                    {
	                        ind_B++;
	                        _dTest_Data1[k][iFeature_Num + 1] = 2;
	                        _dTest_Data1[k][iFeature_Num + 2] = 1;
	                    }
	                    else
	                    {
	                        ind_D++;
	                        _dTest_Data1[k][iFeature_Num + 1] = 2;
	                        _dTest_Data1[k][iFeature_Num + 2] = 0;
	                    }
	                }
	            }

	            // return test_data1;
	            return _dTest_Data1[0][iFeature_Num + 1];
	            
	        }

	        public void Reset()
	        {
	        	_dTest_Data=ArrayClear(_dTest_Data);
	        	_dTest_Data1=ArrayClear(_dTest_Data1);
	        	_dResult_HB0=ArrayClear(_dResult_HB0);
	        	_dResult_HB1=ArrayClear(_dResult_HB1);
	        	_dSum_HB=ArrayClear(_dSum_HB);
	        }
	    }

	 
	class HyperBox
	 {
	        public double[][][] _dHB_x;
	        public double[][][] _dHB_y;

	        public int _iX;
	        public int _iY;
	        public int _iZ;

	        public HyperBox()
	        {
	        }

//	        ~HyperBox()
//	        {
//	        }

	        public void CreateHyperBox(int x, int y, int z)
	        {
	            _iX = x;
	            _iY = y;
	            _iZ = z;

	            _dHB_x = new double[x][][];
	            _dHB_y = new double[x][][];

	            for (int i = 0; i < x; i++)
	            {
	                _dHB_x[i] = new double[y][];
	                _dHB_y[i] = new double[y][];

	                for (int j = 0; j < y; j++)
	                {
	                    _dHB_x[i][j] = new double[z];
	                    _dHB_y[i][j] = new double[z];
	                }
	            }
	        }
	    }
	
	class VE
    {
        private final int OUTPUT_NUMBER = 6;

        private int _iSample;
        private int _iCoor;
        private int _iPhase;
        private int[] _iPrepro;
        private int[] _iXCoor;
        private int[] _iYCoor;
        private float[] _fOutput;
        private float _fDZero;

        public VE(int iPhase, int iSample, int iCoor)
        {
            _fDZero = 0;
            _iPhase = iPhase;
            _iSample = iSample;
            _iCoor = iCoor;
            _iPrepro = new int[iSample];
            _iXCoor = new int[iCoor];
            _iYCoor = new int[iCoor];
            _fOutput = new float[OUTPUT_NUMBER];
        }

//        ~VE()
//        {
//        }

        public float Process(float[] fECG)
        {
            Preprocess(fECG);
            Calculate();
            return _fDZero;
        }

        private int[] Preprocess(float[] fECG)
        {
            for (int i = 0; i < _iSample; i++)
            {
                fECG[i] = fECG[i] * 100;
            }

            float fMax = Max(fECG);
            float fMin = Min(fECG);

            double da = (_iPhase / (double)(fMax - fMin));

            // 소수 8�?자리에서 반올�?
//            da = Math.round(da);
            	
            float[] rgftran = new float[_iSample];

            for (int i = 0; i < _iSample; i++)
            {
                rgftran[i] = (float)(fECG[i] * da);
            }

            //absolute array min
            double dmintran = Math.abs((int)Min(rgftran));

            //int process
            for (int i = 0; i < _iSample; i++)
            {
                _iPrepro[i] = (int)Math.floor(rgftran[i] + dmintran);
            }

            return _iPrepro;
        }

        private void Calculate()
        {
            Coordinate(_iPrepro);
            DZero(_iXCoor, _iYCoor);
        }

        private void Coordinate(int[] iECG)
        {
            for (int i = 0; i < _iCoor; i++)
            {
                _iXCoor[i] = iECG[i];
            }

            for (int i = _iSample - _iCoor; i < _iSample; i++)
            {
                _iYCoor[i - (_iSample - _iCoor)] = iECG[i];
            }
        }

        private float DZero(int[] iXCoo, int[] iYCoo)
        {
            int n = 1, m = 0;

            for (int i = 1; i < _iCoor; i++)
            {

                for (int j = 0; j <= i - 1; j++)
                {
                    if (!(iXCoo[i] == iXCoo[j]) ||
                        ((iXCoo[i] == iXCoo[j]) &
                        (!(iYCoo[i] == iYCoo[j]))))
                    {
                        m += 0;
                    }
                    else
                    {
                        m += 1;
                    }
                }
                if (m == 0)
                {
                    n += 1;
                }

                m = 0;
            }

            return _fDZero = (float)n / (_iPhase * _iPhase);
        }

        public float GetData(int i)
        {
            int temp = (int)(_fOutput[i] * 10000);
            _fOutput[i] = temp / 10000;
            return _fOutput[i];
        }
    }

	 class XMLTableReader
    {
//        private DataSet _DataSet = null;
        private SAXReader reader;
        InputStream xMLInputStream;
        public XMLTableReader(InputStream is)
        {
//            _DataSet = new DataSet();
        	reader=new SAXReader();
        	xMLInputStream=is;
        	
        }
    
    public void LoadXML(InputStream is)
    {
    	if (xMLInputStream!=null) {
			Document document = null;
			try {
				
				document = reader.read(xMLInputStream);
				
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Element root=document.getRootElement();
			for (int flag = 0; flag < 2; flag++) {
				String HBX="HBX"+flag;
				String HBY="HBY"+flag;
				
				
				
				Map<String, Vector<Double>> map1=new HashMap<String, Vector<Double>>();
				for (Iterator k = root.elementIterator(HBX); k.hasNext() ;) {
					//HBX0~1
					for (int i = 0; i < 4; i++) {
						String A="A"+i;
						Element hBX=(Element)k.next();
						
						Vector<Double> attributeVector=new Vector<Double>();
						Element a=hBX.element(A);
						List<DefaultText> attrList=a.content();
						String[] value=attrList.get(0).getText().split(" ");
						for (int j =0; j<value.length;j++) {
							
							attributeVector.add(Double.parseDouble(value[j]) );
						}
						map1.put(A, attributeVector);
						
					}
					hashtable.put(HBX, map1);
//					map.clear();
				}
				
				Map<String, Vector<Double>> map2=new HashMap<String, Vector<Double>>();
				//HBY0~1
				for (Iterator k = root.elementIterator(HBY); k.hasNext() ;) {
					for (int i = 0; i < 4; i++) {
						String A="A"+i;
						Element hBY=(Element)k.next();
						
						Vector<Double> attributeVector=new Vector<Double>();
						Element a=hBY.element(A);
						List<DefaultText> attrList=a.content();
						String[] value=attrList.get(0).getText().split(" ");
						for (int j =0; j<value.length;j++) {
							
							attributeVector.add(Double.parseDouble(value[j]));
						}
					map2.put(A, attributeVector);
						
					}
					hashtable.put(HBY, map2);
//					map.clear();
				}
				
			}
			
			
		}
    	
//        try
//        {
//        	
//            _DataSet.ReadXml(szFilePath);
//        }
//        catch (Exception ex)
//        {
//            throw ex;
//        }
    }
   
        public Map<String, Vector<Double>> GetTable(String szTableName)
        {
            return hashtable.get(szTableName);
        }

        public Map<String, Vector<Double>> GetTable(int iTableIndex)
        {
            return hashtable.get(String.valueOf(iTableIndex));
        }

        public int GetTableCount()
        {
//            return hashtable.size();
        	return 4;
        }
    }
}
