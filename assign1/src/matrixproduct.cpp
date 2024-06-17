#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <fstream>
#include <string>
#include <papi.h>
#include <omp.h>

using namespace std;
#define SYSTEMTIME clock_t

void OnMult(int m_ar, int m_br, ofstream& outputFile) 
{
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

    Time1 = clock();

	for(i=0; i<m_ar; i++) {
        for( j=0; j<m_br; j++){
            temp = 0;
            for( k=0; k<m_ar; k++) {	
                temp += pha[i*m_ar+k] * phb[k*m_br+j];
            }
            phc[i*m_ar+j]=temp;
        }
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	outputFile << st;
    cout << st;
	

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}


void OnMultLine(int m_ar, int m_br, ofstream& outputFile)
{
    SYSTEMTIME Time1, Time2;
    char st[100];
    int i, j, k;

    double *pha, *phb, *phc;
        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);
    
    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;

    Time1 = clock();

    for(i=0; i<m_ar; i++) {
        for( k=0; k<m_ar; k++ ) {
            for( j=0; j<m_br; j++) {    
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
            }
        }
    }

    Time2 = clock();
    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    outputFile << st;
    cout << st;
    
    // display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {   for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}


void OnMultBlock(int m_ar, int m_br, int bkSize, ofstream& outputFile)
{
    SYSTEMTIME Time1, Time2;
    char st[100];
    int a, b, c, i, j, k;

    double *pha, *phb, *phc;
        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);
    
    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;

    Time1 = clock();

    for (a = 0; a < m_ar; a += bkSize) {
        for (c = 0; c < m_ar; c += bkSize) {
            for (b = 0; b < m_br; b += bkSize) {
                for(i = a; i < a + bkSize; i++) {
                    for(k = c; k < c + bkSize; k++ ) {
                        for(j = b; j < b + bkSize; j++) {    
                            phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
                        }
                    }
                }
            }
        }
    }

    Time2 = clock();
    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    outputFile << st;
    cout << st;
    
    // display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {   for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}


void OnMultLineParallel1(int m_ar, int m_br, ofstream& outputFile)
{
    double Time1, Time2;
    char st[100];
    int i, j;

    double *pha, *phb, *phc;
        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);
    
    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;

    Time1 = omp_get_wtime();

    #pragma omp parallel for
    for(int i=0; i<m_ar; i++) {
        for(int  k=0; k<m_ar; k++ ) {
            for(int j=0; j<m_br; j++) {    
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
            }
        }
    }

    Time2 = omp_get_wtime();
    sprintf(st, "Time: %3.3f seconds\n", Time2 - Time1);
    outputFile << st;
    cout << st;
    
    // display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {   for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}


void OnMultLineParallel2(int m_ar, int m_br, ofstream& outputFile)
{
    double Time1, Time2;
    char st[100];
    int i, j;

    double *pha, *phb, *phc;
        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);
    
    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;

    Time1 = omp_get_wtime();

    #pragma omp parallel
    for(int i=0; i<m_ar; i++) {
        for(int k=0; k<m_ar; k++ ) {
            #pragma omp for
            for(int j=0; j<m_br; j++) {    
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
            }
        }
    }

    Time2 = omp_get_wtime();
    sprintf(st, "Time: %3.3f seconds\n", Time2 - Time1);
    outputFile << st;
    cout << st;
    
    // display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {   for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}


void handle_error(int retval)
{
    printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
    exit(1);
}

int main(int argc, char *argv[])
{   
    int op = atoi(argv[1]);

    ofstream f;
    f.open(op == 3 ? argv[4] : argv[3], ios::app);

    int matrix_size = atoi(argv[2]);

	int size = atoi(argv[2]);
    
    // PAPI setup
    int EventSet = PAPI_NULL;
    long long values[3];
    int ret;
    ret = PAPI_library_init(PAPI_VER_CURRENT);
    if (ret != PAPI_VER_CURRENT)
    {
        cout << "FAIL" << endl;
    }

    ret = PAPI_create_eventset(&EventSet);
    if (ret != PAPI_OK)
        cout << "ERROR: create eventset" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L1_DCM);
    if (ret != PAPI_OK)
        cout << "ERROR: PAPI_L1_DCM" << endl;

    ret = PAPI_add_event(EventSet, PAPI_L2_DCM);
    if (ret != PAPI_OK)
        cout << "ERROR: PAPI_L2_DCM" << endl;

    ret = PAPI_start(EventSet);
	if (ret != PAPI_OK)
		cout << "ERROR: Start PAPI" << endl;

    switch (op)
    {
    case 1:
		OnMult(size, size, f);	
        break;
    case 2:
		OnMultLine(size, size, f);            
        break;
    case 3:
    {
        int block_size = atoi(argv[3]);
        cout << "Block size: " << block_size << endl;
        f << "Block size: " << block_size << endl;
        OnMultBlock(size, size, block_size, f);
        break;
    }
    case 4:
        OnMultLineParallel1(size, size, f);
        break;
    case 5:
        OnMultLineParallel2(size, size, f);
        break;
    default:
        cout << "Invalid option" << endl;
        break;
    }

    ret = PAPI_stop(EventSet, values);
	if (ret != PAPI_OK)
	    cout << "ERROR: Stop PAPI" << endl;

	printf("L1 DCM: %lld \n", values[0]);
	printf("L2 DCM: %lld \n", values[1]);


	f << "Dimensions: " << matrix_size << "\n";
	f << "L1 DCM:" <<  values[0] << "\n";
	f << "L2 DCM:" <<  values[1] << "\n\n";

    f.close();

    ret = PAPI_reset(EventSet);
    if (ret != PAPI_OK)
        cout << "FAIL reset" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L1_DCM);
    if (ret != PAPI_OK)
        cout << "FAIL remove event" << endl;

    ret = PAPI_remove_event(EventSet, PAPI_L2_DCM);
    if (ret != PAPI_OK)
        cout << "FAIL remove event" << endl;

    ret = PAPI_destroy_eventset(&EventSet);
    if (ret != PAPI_OK)
        cout << "FAIL destroy" << endl;

    return 0;
}
