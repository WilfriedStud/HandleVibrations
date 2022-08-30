package ro.usv.function;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import ro.usv.function.EventSchema.EventSchemaData.Body.Data.Vector3;

public class HandleNotifications {

    @FunctionName("handleNotifications")
    public void run(
            @EventGridTrigger(name = "event") EventSchema event,
            final ExecutionContext context) {

        List<Vector3> data = extractDataFromEventSchema(event);
        List<Vector3> displacementList = computeDisplacements(data);

        String timeRange = timeRange(event);
        context.getLogger().info(timeRange);

        Vector3 averageDisplacement = averageDisplacement(displacementList);
        context.getLogger().info("Average displacement x: " + averageDisplacement.x);
        context.getLogger().info("Average displacement y: " + averageDisplacement.y);
        context.getLogger().info("Average displacement z: " + averageDisplacement.z);

        double max = Double.parseDouble(System.getenv("threshold"));

        StringBuilder messageBuilder = new StringBuilder();

        if (averageDisplacement.x > max) {
            context.getLogger().info("threshold passed on x");
            messageBuilder.append(String.format("%s(%.2f); ", "x", averageDisplacement.x));
        }
        if (averageDisplacement.y > max) {
            context.getLogger().info("threshold passed on y");
            messageBuilder.append(String.format("%s(%.2f); ", "y", averageDisplacement.y));
        }
        if (averageDisplacement.z > max) {
            context.getLogger().info("threshold passed on z");
            messageBuilder.append(String.format("%s(%.2f); ", "z", averageDisplacement.z));
        }
        if (messageBuilder.length() == 0) {
            context.getLogger().info("No vibration detelcted");
        } else {
            messageBuilder.insert(0, "Notification: ").append("Timerange(").append(timeRange).append(')');
            new AzureIotClient(context.getLogger()).sendMessage(messageBuilder.toString());
        }

    }

    private String timeRange(EventSchema event) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        Date startRange = new Date(event.data.body.telemetry.get(0).timestamp);
        Date endRange = new Date(event.data.body.telemetry.get(0).timestamp);
        return formatter.format(startRange) + " <=> " + formatter.format(endRange);
    }

    private List<Vector3> extractDataFromEventSchema(EventSchema event) {
        return event.data.body.telemetry.stream().sequential().map(t -> t.data).collect(Collectors.toList());
    }

    private List<Vector3> computeDisplacements(List<Vector3> data) {
        List<Vector3> displacementList = new ArrayList<>(data.size() - 1);

        for (int i = 0; i < data.size() - 1; i++) {
            Vector3 displacement = new Vector3();
            displacement.x = Math.abs(data.get(i).x - data.get(i + 1).x);
            displacement.y = Math.abs(data.get(i).y - data.get(i + 1).y);
            displacement.z = Math.abs(data.get(i).z - data.get(i + 1).z);
            displacementList.add(displacement);
        }
        return displacementList;
    }

    private Vector3 averageDisplacement(List<Vector3> displacementList) {
        Vector3 displacement = new Vector3();
        displacement.x = displacementList.stream().mapToDouble(d -> d.x).average().orElse(.0);
        displacement.y = displacementList.stream().mapToDouble(d -> d.y).average().orElse(.0);
        displacement.z = displacementList.stream().mapToDouble(d -> d.z).average().orElse(.0);
        return displacement;
    }
}