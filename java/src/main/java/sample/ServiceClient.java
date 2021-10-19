package sample;

import com.google.protobuf.ByteString;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceClient {
    private static final Logger logger = Logger.getLogger(ServiceClient.class.getName());

    private final SampleServiceGrpc.SampleServiceBlockingStub blockingStub;
    private final SampleServiceGrpc.SampleServiceStub asyncStub;

    /** construct client for accessing Service server using existing channel */
    public ServiceClient(Channel channel) {
        blockingStub = SampleServiceGrpc.newBlockingStub(channel);
        asyncStub = SampleServiceGrpc.newStub(channel);
    }

    /** add product information to server */
    public void addProductInfo() throws UnsupportedEncodingException {
        logger.info("add product information to server");
        ByteString data = ByteString.copyFrom("client byte data", "utf-8");
        ProductInfo request = ProductInfo.newBuilder()
                .setId("1")
                .setName("apple")
                .setDescription("Things of Fruit")
                .setPrice(1000)
                .setData(data)
                .build();
        ProductID response;
        try {
            response = blockingStub.addProductInfo(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "addProductInfo failed: {0}", e.getStatus());
            return;
        }
        logger.info("success to add product id " + response.getId());
    }

    /** get product information from server using product id */
    public void getProductInfo(String id) {
        logger.info("get product information to server");
        ProductID request = ProductID.newBuilder()
                .setId(id)
                .build();
        ProductInfo response;
        try {
            response = blockingStub.getProductInfo(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "getProductInfo failed: {0}", e.getStatus());
            return;
        }
        Integer price = response.getPrice();
        logger.info("success to get product info \nid: " + response.getId() +
                "\nname: " + response.getName() +
                "\nprice: " + price +
                "\ndata: " + response.getData().toString());
    }

    public void streamingClient(int interation) {
        StreamObserver<SingleResponse> responseObserver = new StreamObserver<SingleResponse>() {
            @Override
            public void onNext(SingleResponse response) {
                logger.info("speech type: " + response.getSpeechEventType() + "\n" +
                        "result" + response.getResult());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "client side failed", Status.fromThrowable(t));
            }

            @Override
            public void onCompleted() {
                logger.info("client side streaming service finished");
            }
        };

        StreamObserver<StreamingRequest> requestObserver = asyncStub.streamingClient(responseObserver);
        try {
            for (int i = 0; i < interation; ++i) {
                ByteString content = ByteString.copyFromUtf8("client request");
                StreamingRequest request = StreamingRequest.newBuilder()
                        .setEncoding("pcm16")
                        .setSampleRateHertz(16000)
                        .setSingleUtterance(true)
                        .setInterimResult(false)
                        .setReturnFlag(false)
                        .setNumChannels(1)
                        .setAudioContent(content)
                        .build();
                requestObserver.onNext(request);
                logger.info("client request give!!");
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();
    }

    public static void main(String[] args) throws Exception {
        String target = "localhost:5556";

        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        try {
            ServiceClient client = new ServiceClient(channel);
            client.addProductInfo();
            client.getProductInfo("1");
            client.streamingClient(2);
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
