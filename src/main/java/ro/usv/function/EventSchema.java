package ro.usv.function;

import java.util.List;

public class EventSchema {
  public EventSchemaData data;

  @Override
  public String toString() {
    return "EventSchema{" +
        "data=" + data +
        '}';
  }

  public static class EventSchemaData {
    public Body body;

    @Override
    public String toString() {

      return "EventSchemaData{" +
          "body=" + body +
          '}';
    }

    public static class Body {
      String sensorType;
      List<Data> telemetry;

      @Override
      public String toString() {

        return "Body{" +
            "sensorType='" + sensorType + '\'' +
            ", telemetry=" + telemetry +
            '}';
      }

      public static class Data {
        public Long timestamp;
        public Vector3 data;

        @Override
        public String toString() {

          return "Data{" +
              "timestamp=" + timestamp +
              ", data=" + data +
              '}';
        }

        public static class Vector3 {
          public Double x;
          public Double y;
          public Double z;

          @Override
          public String toString() {

            return "Vector3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
          }
        }
      }
    }
  }
}