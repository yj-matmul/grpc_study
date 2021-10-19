package sample;

import com.google.protobuf.ByteString;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceServer {
    private static final Logger logger = Logger.getLogger(ServiceServer.class.getName());

    private Server server;
    private static ByteString data;

    static {
        try {
            data = ByteString.copyFrom("server byte data", "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public ServiceServer() throws UnsupportedEncodingException {
    }

    /** server start */
    private void start() throws IOException {
        int port = 5556;
        server = ServerBuilder.forPort(port)
                .addService(new ServiceServerImpl())
                .build()
                .start();
        logger.info("Server starting, listening on " + port);
    }

    /** implement gRPC methods */
    private static class ServiceServerImpl extends SampleServiceGrpc.SampleServiceImplBase  {
        /** add product information from client */
        public void addProductInfo(ProductInfo req, StreamObserver<ProductID> responseObserver) {
            ProductID productID = ProductID.newBuilder()
                    .setId(req.getId())
                    .build();
            Integer price = req.getPrice();
            logger.info("get product info \nid: " + req.getId() +
                    "\nname: " + req.getName() +
                    "\nprice: " + price +
                    "\ndata: " + req.getData());
            responseObserver.onNext(productID);
            responseObserver.onCompleted();
        }

        /** return product information to client by product id*/
        public void getProductInfo(ProductID req, StreamObserver<ProductInfo> responseObserver) {
            ProductInfo productInfo = ProductInfo.newBuilder()
                    .setId(req.getId())
                    .setName("apple")
                    .setDescription("Things of Fruit")
                    .setPrice(1000)
                    .setData(data)
                    .build();
            logger.info("give product info of id " + req.getId());
            responseObserver.onNext(productInfo);
            responseObserver.onCompleted();
        }

        /** get stream request from client and return single response */
        public StreamObserver<StreamingRequest> StreamingClient(final StreamObserver<SingleResponse> responseObserver) {
            return new StreamObserver<StreamingRequest>() {
                @Override
                public void onNext(StreamingRequest request) {
                    String content = request.getAudioContent().toString();
                    logger.info("server onNext" + content);
                }

                @Override
                public void onError(Throwable t) {
                    logger.log(Level.WARNING, "encounter client error on server", t);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onNext(SingleResponse.newBuilder()
                    .setSpeechEventType("utterance")
                    .setResult("response success")
                    .setScore(1.5F)
                    .setNToken(5)
                    .setStartTime(1.0F)
                    .setEndTime(2.0F)
                    .build());
                    responseObserver.onCompleted();
                }
            };
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ServiceServer server = new ServiceServer();
        server.start();
        server.blockUntilShutdown();
    }
}
