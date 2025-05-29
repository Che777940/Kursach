package Server;

import Server.Config.DatabaseConfig;
import Server.Handlers.UnifiedClientHandler;
import Server.Services.DatabaseService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Main {
   private static final Logger logger = Logger.getLogger(Main.class.getName());

   public static void main(String[] args) {
      try (DatabaseService dbService = new DatabaseService()) {
         logger.info("Подключение к базе данных MySQL установлено.");

         try (ServerSocket serverSocket = new ServerSocket(DatabaseConfig.PORT)) {
            logger.info("Сервер запущен на порту " + DatabaseConfig.PORT);

            while (true) {
               Socket clientSocket = serverSocket.accept();
               new UnifiedClientHandler(clientSocket, dbService).start();
            }
         }
      } catch (SQLException | IOException e) {
         logger.severe("Ошибка: " + e.getMessage());
      }
   }
}