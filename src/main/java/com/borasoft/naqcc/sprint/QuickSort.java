package com.borasoft.naqcc.sprint;

public final class QuickSort {
  
  public LogEntry[] quickSort(LogEntry[] data) {
    int lenD = data.length;
    //int pivot = 0;
    LogEntry pivotEntry = null; // this needs to be reviewed.
    int ind = lenD/2;
    int i,j = 0,k = 0;
    if(lenD<2) {
      return data;
    } else {
      LogEntry[] L = new LogEntry[lenD];
      LogEntry[] R = new LogEntry[lenD];
      LogEntry[] sorted = new LogEntry[lenD];
      pivotEntry = data[ind];
      for(i=0;i<lenD;i++) {
        if(i!=ind) {
          if(data[i].getFinal()<pivotEntry.getFinal()) {
            L[j] = data[i];
            j++;
          } else {
            R[k] = data[i];
            k++;
          }
        }
      }
      
      LogEntry[] sortedL = new LogEntry[j];
      LogEntry[] sortedR = new LogEntry[k];
      System.arraycopy(L, 0, sortedL, 0, j);
      System.arraycopy(R, 0, sortedR, 0, k);
      sortedL = quickSort(sortedL);
      sortedR = quickSort(sortedR);
      System.arraycopy(sortedL, 0, sorted, 0, j);
      sorted[j] = pivotEntry;     
      System.arraycopy(sortedR, 0, sorted, j+1, k);
      return sorted;
    }
  }
}
