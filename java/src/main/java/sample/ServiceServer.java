package sample;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

public class ServiceServer {
    private static final Logger logger = Logger.getLogger(ServiceServer.class.getName());

    private Server server;

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
    private static class ServiceServerImpl extends SampleServiceGrpc.SampleServiceImplBase {
        /** add product information from client */
        public void addProductInfo(ProductInfo req, StreamObserver<ProductID> responseObserver) {
            ProductID productID = ProductID.newBuilder()
                    .setId(req.getId())
                    .build();
            logger.info("get product id " + req.getId() + " and add product info");
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
                    .build();
            logger.info("give product info of id " + req.getId());
            responseObserver.onNext(productInfo);
            responseObserver.onCompleted();
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
