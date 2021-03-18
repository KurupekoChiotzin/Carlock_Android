package com.mulin.larlock.larlock;

public class DataTransform {
        /** Byte轉 Hex String**/
        public static String byteArrayToHexStr(byte[] byteArray) {
            if (byteArray == null) {
                return null;
            }
            char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[byteArray.length * 2];
            for (int j = 0; j < byteArray.length; j++) {
                int v = byteArray[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }

        /**str轉ByteArray**/
        public static byte[] strToByteArray(String str){
            if(str==null){
                return null;
            }
            byte[] byteArray=str.getBytes();
            return byteArray;
        }

        /*byte轉str*/
        public static String byteArrayToStr(byte[] byteArray) {
            if (byteArray == null) {
                return null;
            }
            String str = new String(byteArray);
            return str;
        }
}
