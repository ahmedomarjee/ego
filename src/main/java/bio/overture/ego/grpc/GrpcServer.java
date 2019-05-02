package bio.overture.ego.grpc;

import bio.overture.ego.grpc.interceptor.AuthInterceptor;
import bio.overture.ego.grpc.service.UserServiceGrpcImpl;
import io.grpc.*;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("grpc")
public class GrpcServer implements CommandLineRunner, DisposableBean {

  @Value("${grpc.port}")
  private int port;

  private Server server;

  private final AuthInterceptor authInterceptor;
  private final UserServiceGrpcImpl userServiceImpl;

  @Autowired
  public GrpcServer(AuthInterceptor authInterceptor, UserServiceGrpcImpl userServiceImpl) {

    this.authInterceptor = authInterceptor;

    this.userServiceImpl = userServiceImpl;
  }

  @Override
  public void run(String... args) throws Exception {

    val userService = ServerInterceptors.intercept(userServiceImpl, authInterceptor);

    server =
        ServerBuilder.forPort(port)
            .addService(userService)
            //            .addService(ProtoReflectionService.newInstance())
            .build()
            .start();

    log.info("gRPC Server started, listening on " + port);
    startDaemonAwaitThread();
  }

  private void startDaemonAwaitThread() {
    Thread awaitThread =
        new Thread(
            () -> {
              try {
                this.server.awaitTermination();
              } catch (InterruptedException e) {
                log.error("gRPC server stopped.", e);
              }
            });
    awaitThread.start();
  }

  @Override
  public final void destroy() throws Exception {
    log.info("Shutting down gRPC server ...");
    Optional.ofNullable(server).ifPresent(Server::shutdown);
    log.info("gRPC server stopped.");
  }
}
