package ro.usv.function;

import java.util.List;

public class DataPack {

    public String sensorType;
    public List<Data> data;


    public static class Data {
        public String timestamp;
        public Vector3 data;

        public static class Vector3 {
            public Integer x;
            public Integer y;
            public Integer z;
        } 
    }
}