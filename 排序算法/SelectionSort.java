

/**
 * 选择排序：最简单的排序算法，
 */

public public class SelectionSort {
    public void Sort(int[] array) {
        int  temp = 0;

        for(int j = 0;j < array.length;j++){
            for(int i = 0;i < array.length;){
                if(temp <= array[i]){
                    i++;
                }else{
                    temp = array[i];
                }
            }
        }
        
    }
}