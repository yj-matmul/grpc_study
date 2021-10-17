package sample;

import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceClient {
    private static final Logger logger = Logger.getLogger(ServiceClient.class.getName());

    private final SampleServiceGrpc.SampleServiceBlockingStub blockingStub;

    /** construct client for accessing Service server using existing channel */
    public ServiceClient(Channel channel) {
        blockingStub = SampleServiceGrpc.newBlockingStub(channel);
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

    public static void main(String[] args) throws Exception {
        String target = "localhost:5556";

        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        try {
            ServiceClient client = new ServiceClient(channel);
            client.addProductInfo();
            client.getProductInfo("1");
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
