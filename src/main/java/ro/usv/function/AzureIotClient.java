package ro.usv.function;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement;
import com.microsoft.azure.sdk.iot.service.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.FeedbackReceiver;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

public class AzureIotClient {

    private static final String CONNECTION_STRING = "HostName=willy-iot.azure-devices.net;SharedAccessKeyName=service;SharedAccessKey=ygqfXbvJtTjhtzVOY98GDX3dMKIIlYMtTBRw4hTxgqY=";
    private static final String DEVICE_ID = "iotdev1";

    private final ServiceClient client = new ServiceClient(CONNECTION_STRING, IotHubServiceClientProtocol.AMQPS);
    private final Logger log;

    public AzureIotClient(Logger log) {
        this.log = log;
    }

    public void sendMessage(String message) {

        try {
            log.info("Opening service client");
            if (Objects.nonNull(client)) {
                client.open();
                FeedbackReceiver feedbackReceiver = openFeedbackReceiver(client);
                Message notification = new Message(message);
                notification.setDeliveryAcknowledgementFinal(DeliveryAcknowledgement.Full);
                deliverToDevice(notification, client);
                retreiveConfirmation(feedbackReceiver);
                closeResources(feedbackReceiver, client);
            }
        } catch (IOException ioe) {
            log.warning("Exception during opening of service client" + ioe.getMessage());
        }
    }

    private void closeResources(FeedbackReceiver feedbackReceiver, ServiceClient client) {
        try {
            log.info("Closing resources");
            if (Objects.nonNull(feedbackReceiver)) {
                feedbackReceiver.close();
            }
            client.close();
        } catch (IOException ioe) {
            log.warning("Exception when trying to close resources" + ioe.getMessage());
        }
    }

    private void retreiveConfirmation(FeedbackReceiver feedbackReceiver) {
        try {
            log.info("Receiving feedback confirmation");
            FeedbackBatch feedbackBatch = feedbackReceiver.receive(10000);
            if (Objects.nonNull(feedbackBatch)) {
                log.info("Feedback received at " + feedbackBatch.getEnqueuedTimeUtc().toString());
            }
        } catch (IOException | InterruptedException e) {
            log.warning("Exception during feedback waiting" + e.getMessage());
        }
    }

    private void deliverToDevice(Message notification, ServiceClient client) {
        try {
            log.info("Sending message to iot hub");
            client.send(DEVICE_ID, notification);
        } catch (IOException | IotHubException e) {
            log.warning("Exception during sending of message to iot hub" + e.getMessage());
        }
    }

    private FeedbackReceiver openFeedbackReceiver(ServiceClient client) {
        FeedbackReceiver feedbackReceiver = client.getFeedbackReceiver();
        try {
            log.info("Opening feedback receiver");
            if (Objects.nonNull(feedbackReceiver)) {
                feedbackReceiver.open();
            }
        } catch (IOException ioe) {
            log.warning("Exception during opening of feedback receiver" + ioe.getMessage());
        }
        return feedbackReceiver;
    }

}