package Server.Handlers;

import Server.Controllers.AuthController;
import Server.Controllers.ClientController;
import Server.Controllers.FicoParameterController;
import Server.Controllers.FinancialController;
import Server.Controllers.CreditApplicationController;
import Server.Controllers.SecurityLogsController;
import Server.Models.CreditApplication;
import Server.Models.Client;
import Server.Models.FicoParameter;
import Server.Models.FinancialInfo;
import Server.Models.SecurityLog;
import Server.Models.User;
import Server.Services.DatabaseService;
import Server.Utils.RequestParser;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnifiedClientHandler extends Thread {
    private static final Logger logger = Logger.getLogger(UnifiedClientHandler.class.getName());
    private final Socket socket;
    private final AuthController authController;
    private final ClientController clientController;
    private final FicoParameterController ficoController;
    private final FinancialController financialController;
    private final CreditApplicationController creditApplicationController;
    private final SecurityLogsController securityLogsController;

    public UnifiedClientHandler(Socket socket, DatabaseService dbService) {
        this.socket = socket;
        this.authController = new AuthController(dbService);
        this.clientController = new ClientController(dbService);
        this.ficoController = new FicoParameterController(dbService);
        this.financialController = new FinancialController(dbService, ficoController);
        this.creditApplicationController = new CreditApplicationController(dbService);
        this.securityLogsController = new SecurityLogsController(dbService);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request = in.readLine();
            if (request == null || request.trim().isEmpty()) {
                logger.warning("Получен пустой запрос от клиента");
                out.println("ERROR: Пустой запрос.");
                return;
            }

            logger.info("Получен запрос: " + request);
            List<String> parts = RequestParser.parseRequest(request);
            if (parts.isEmpty()) {
                logger.warning("Невозможно разобрать запрос: " + request);
                out.println("ERROR: Неверный формат запроса.");
                return;
            }

            String command = parts.get(0).toUpperCase();
            switch (command) {
                case "LOGIN":
                    handleLogin(parts, out);
                    break;
                case "REGISTER":
                    handleRegister(parts, out);
                    break;
                case "ADD_CLIENT":
                    handleAddClient(parts, out);
                    break;
                case "GET_ALL_USERS":
                    handleGetAllUsers(out);
                    break;
                case "GET_USER_DETAILS":
                    handleGetUserDetails(parts, out);
                    break;
                case "FIND_USER_BY_ID":
                    handleFindUserById(parts, out);
                    break;
                case "UPDATE_USER":
                    handleUpdateUser(parts, out);
                    break;
                case "DELETE_USER":
                    handleDeleteUser(parts, out);
                    break;
                case "GET_FICO_PARAMS":
                    handleGetFicoParams(out);
                    break;
                case "UPDATE_FICO_PARAM":
                    handleUpdateFicoParam(parts, out);
                    break;
                case "RESET_FICO_DEFAULTS":
                    handleResetFicoDefaults(out);
                    break;
                case "CALCULATE_FICO":
                    handleCalculateFico(out);
                    break;
                case "GET_CLIENTS":
                    handleGetClients(out);
                    break;
                case "GET_FINANCIAL_INFO":
                    handleGetFinancialInfo(parts, out);
                    break;
                case "ADD_FINANCIAL_INFO":
                    handleAddFinancialInfo(parts, out);
                    break;
                case "UPDATE_FINANCIAL_INFO":
                    handleUpdateFinancialInfo(parts, out);
                    break;
                case "DELETE_FINANCIAL_INFO":
                    handleDeleteFinancialInfo(parts, out);
                    break;
                case "GET_ALL_CREDIT_APPLICATIONS":
                    handleGetAllCreditApplications(out);
                    break;
                case "GET_CREDIT_APPLICATIONS":
                    handleGetCreditApplications(parts, out);
                    break;
                case "ADD_CREDIT_APPLICATION":
                    handleAddCreditApplication(parts, out);
                    break;
                case "UPDATE_CREDIT_APPLICATION_STATUS":
                    handleUpdateCreditApplicationStatus(parts, out);
                    break;
                case "DELETE_CREDIT_APPLICATION":
                    handleDeleteCreditApplication(parts, out);
                    break;
                case "GET_CLIENT_FINANCIAL_INFO":
                    handleGetClientFinancialInfo(parts, out);
                    break;
                case "CALCULATE_FICO_FOR_CLIENT":
                    handleCalculateFicoForClient(parts, out);
                    break;
                case "GET_SECURITY_LOGS":
                    handleGetSecurityLogs(parts, out);
                    break;
                case "CLEAR_OLD_LOGS":
                    handleClearOldLogs(parts, out);
                    break;
                default:
                    logger.warning("Неизвестная команда: " + command);
                    out.println("ERROR: Неизвестная команда: " + command);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка связи с клиентом: " + e.getMessage(), e);
        } finally {
            try {
                socket.close();
                logger.info("Сокет клиента закрыт");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Ошибка закрытия сокета: " + e.getMessage(), e);
            }
        }
    }

    // ======== Методы для пользователей и клиентов ========

    private void handleLogin(List<String> parts, PrintWriter out) {
        if (parts.size() != 3) {
            logger.warning("Неверный формат команды LOGIN: " + String.join(" ", parts));
            out.println("ERROR: LOGIN username password");
            return;
        }
        try {
            String username = parts.get(1);
            String password = parts.get(2);
            String result = authController.login(username, password);
            logger.info("Результат авторизации для " + username + ": " + result);
            out.println(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при авторизации: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleRegister(List<String> parts, PrintWriter out) {
        if (parts.size() != 4) {
            logger.warning("Неверный формат команды REGISTER: " + String.join(" ", parts));
            out.println("ERROR: REGISTER username password role");
            return;
        }
        try {
            User user = new User(parts.get(1), parts.get(2), parts.get(3));
            String result = authController.register(user);
            logger.info("Результат регистрации для " + user.getUsername() + ": " + result);
            out.println(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при регистрации: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleAddClient(List<String> parts, PrintWriter out) {
        if (parts.size() != 15) {
            logger.warning("Неверный формат команды ADD_CLIENT: " + String.join(" ", parts));
            out.println("ERROR: ADD_CLIENT fullName birthDate gender phone email address income status workplace assets passportNumber passportIssueDate passportExpiryDate passportIssuingCountry");
            return;
        }
        try {
            Client client = new Client(
                    parts.get(1), // fullName
                    LocalDate.parse(parts.get(2)), // birthDate
                    parts.get(3), // gender
                    parts.get(4).equals("NULL") ? null : parts.get(4), // phone
                    parts.get(5).equals("NULL") ? null : parts.get(5), // email
                    parts.get(6).equals("NULL") ? null : parts.get(6), // address
                    parts.get(7).equals("NULL") ? null : new BigDecimal(parts.get(7)), // monthlyIncome
                    parts.get(8).equals("NULL") ? null : parts.get(8), // employmentStatus
                    parts.get(9).equals("NULL") ? null : parts.get(9), // workplace
                    parts.get(10).equals("NULL") ? null : parts.get(10), // assets
                    parts.get(11).equals("NULL") ? null : parts.get(11), // passportNumber
                    parts.get(12).equals("NULL") ? null : LocalDate.parse(parts.get(12)), // passportIssueDate
                    parts.get(13).equals("NULL") ? null : LocalDate.parse(parts.get(13)), // passportExpiryDate
                    parts.get(14).equals("NULL") ? null : parts.get(14) // passportIssuingCountry
            );
            String result = clientController.addClient(client);
            logger.info("Результат добавления клиента " + client.getFullName() + ": " + result);
            out.println(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении клиента: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleGetAllUsers(PrintWriter out) {
        try {
            List<Client> clients = clientController.getAllClients();
            if (clients.isEmpty()) {
                logger.info("Список клиентов пуст");
                out.println("ERROR: Нет клиентов.");
                return;
            }
            StringBuilder response = new StringBuilder("OK");
            for (Client client : clients) {
                response.append("|")
                        .append(client.getId()).append(":")
                        .append(client.getFullName()).append(":")
                        .append(client.getBirthDate()).append(":")
                        .append(client.getGender()).append(":")
                        .append(client.getPhone() != null ? client.getPhone() : "").append(":")
                        .append(client.getEmail() != null ? client.getEmail() : "").append(":")
                        .append(client.getAddress() != null ? client.getAddress() : "").append(":")
                        .append(client.getMonthlyIncome() != null ? client.getMonthlyIncome() : "0.00").append(":")
                        .append(client.getEmploymentStatus() != null ? client.getEmploymentStatus() : "").append(":")
                        .append(client.getWorkplace() != null ? client.getWorkplace() : "").append(":")
                        .append(client.getAssets() != null ? client.getAssets() : "").append(":")
                        .append(client.getPassportNumber() != null ? client.getPassportNumber() : "").append(":")
                        .append(client.getPassportIssueDate() != null ? client.getPassportIssueDate() : "").append(":")
                        .append(client.getPassportExpiryDate() != null ? client.getPassportExpiryDate() : "").append(":")
                        .append(client.getPassportIssuingCountry() != null ? client.getPassportIssuingCountry() : "");
            }
            logger.info("Отправлен список всех клиентов: " + response);
            out.println(response.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении списка клиентов: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleGetUserDetails(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды GET_USER_DETAILS: " + String.join(" ", parts));
            out.println("ERROR: GET_USER_DETAILS userId");
            return;
        }
        try {
            int id = Integer.parseInt(parts.get(1));
            Client client = clientController.getClientById(id);
            if (client == null) {
                logger.warning("Клиент с ID " + id + " не найден");
                out.println("ERROR: Пользователь не найден");
                return;
            }
            String response = String.format("OK|%d|%s|%s|%s|%s|%s|%s|%.2f|%s|%s|%s|%s|%s|%s|%s",
                    client.getId(),
                    client.getFullName(),
                    client.getBirthDate(),
                    client.getGender(),
                    client.getPhone() != null ? client.getPhone() : "",
                    client.getEmail() != null ? client.getEmail() : "",
                    client.getAddress() != null ? client.getAddress() : "",
                    client.getMonthlyIncome() != null ? client.getMonthlyIncome() : BigDecimal.ZERO,
                    client.getEmploymentStatus() != null ? client.getEmploymentStatus() : "",
                    client.getWorkplace() != null ? client.getWorkplace() : "",
                    client.getAssets() != null ? client.getAssets() : "",
                    client.getPassportNumber() != null ? client.getPassportNumber() : "",
                    client.getPassportIssueDate() != null ? client.getPassportIssueDate() : "",
                    client.getPassportExpiryDate() != null ? client.getPassportExpiryDate() : "",
                    client.getPassportIssuingCountry() != null ? client.getPassportIssuingCountry() : "");
            logger.info("Отправлены данные клиента с ID " + id + ": " + response);
            out.println(response);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат ID: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат ID: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении данных клиента: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleFindUserById(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды FIND_USER_BY_ID: " + String.join(" ", parts));
            out.println("ERROR: FIND_USER_BY_ID userId");
            return;
        }
        try {
            int id = Integer.parseInt(parts.get(1));
            Client client = clientController.getClientById(id);
            if (client == null) {
                logger.warning("Клиент с ID " + id + " не найден");
                out.println("ERROR: Пользователь с ID " + id + " не найден");
                return;
            }
            String response = "OK|" + client.getId() + "|" + client.getFullName();
            logger.info("Найден клиент с ID " + id + ": " + response);
            out.println(response);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат ID: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат ID: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при поиске клиента: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleUpdateUser(List<String> parts, PrintWriter out) {
        if (parts.size() != 16) {
            logger.warning("Неверный формат команды UPDATE_USER: " + String.join(" ", parts));
            out.println("ERROR: UPDATE_USER id fullName birthDate gender phone email address income status workplace assets passportNumber passportIssueDate passportExpiryDate passportIssuingCountry");
            return;
        }
        try {
            Client client = new Client(
                    Integer.parseInt(parts.get(1)), // id
                    parts.get(2), // fullName
                    LocalDate.parse(parts.get(3)), // birthDate
                    parts.get(4), // gender
                    parts.get(5).equals("NULL") ? null : parts.get(5), // phone
                    parts.get(6).equals("NULL") ? null : parts.get(6), // email
                    parts.get(7).equals("NULL") ? null : parts.get(7), // address
                    parts.get(8).equals("NULL") ? null : new BigDecimal(parts.get(8)), // monthlyIncome
                    parts.get(9).equals("NULL") ? null : parts.get(9), // employmentStatus
                    parts.get(10).equals("NULL") ? null : parts.get(10), // workplace
                    parts.get(11).equals("NULL") ? null : parts.get(11), // assets
                    parts.get(12).equals("NULL") ? null : parts.get(12), // passportNumber
                    parts.get(13).equals("NULL") ? null : LocalDate.parse(parts.get(13)), // passportIssueDate
                    parts.get(14).equals("NULL") ? null : LocalDate.parse(parts.get(14)), // passportExpiryDate
                    parts.get(15).equals("NULL") ? null : parts.get(15) // passportIssuingCountry
            );
            String result = clientController.updateClient(client);
            logger.info("Результат обновления клиента с ID " + client.getId() + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат числа: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат числа: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении клиента: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleDeleteUser(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды DELETE_USER: " + String.join(" ", parts));
            out.println("ERROR: DELETE_USER userId");
            return;
        }
        try {
            int id = Integer.parseInt(parts.get(1));
            String result = clientController.deleteClient(id);
            logger.info("Результат удаления клиента с ID " + id + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат ID: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат ID: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при удалении клиента: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    // ======== Методы для параметров FICO ========

    private void handleGetFicoParams(PrintWriter out) {
        try {
            List<FicoParameter> params = ficoController.getAllParameters();
            if (params.isEmpty()) {
                logger.info("Список параметров FICO пуст");
                out.println("ERROR: Нет параметров FICO.");
                return;
            }
            StringBuilder response = new StringBuilder("OK");
            for (FicoParameter param : params) {
                response.append(String.format("|%d:%s:%d:%s",
                        param.getId(),
                        param.getParameterName(),
                        param.getWeightPercentage(),
                        param.getDescription()));
            }
            logger.info("Отправлен список параметров FICO: " + response);
            out.println(response.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении параметров FICO: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleUpdateFicoParam(List<String> parts, PrintWriter out) {
        if (parts.size() != 4) {
            logger.warning("Неверный формат команды UPDATE_FICO_PARAM: " + String.join(" ", parts));
            out.println("ERROR: UPDATE_FICO_PARAM id weight value");
            return;
        }
        try {
            int id = Integer.parseInt(parts.get(1));
            int weight = Integer.parseInt(parts.get(2));
            int value = Integer.parseInt(parts.get(3)); // Поле не используется, но оставлено для совместимости
            FicoParameter param = ficoController.getParameterById(id);
            if (param == null) {
                logger.warning("Параметр FICO с ID " + id + " не найден");
                out.println("ERROR: Параметр с ID " + id + " не найден");
                return;
            }
            param.setWeightPercentage(weight);
            param.setCalculatedValue(value);
            String result = ficoController.updateParameter(param);
            logger.info("Результат обновления параметра FICO с ID " + id + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат числа: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат числа: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении параметра FICO: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleResetFicoDefaults(PrintWriter out) {
        try {
            String result = ficoController.resetToDefault();
            logger.info("Результат сброса параметров FICO: " + result);
            out.println(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при сбросе параметров FICO: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleCalculateFico(PrintWriter out) {
        logger.warning("Команда CALCULATE_FICO устарела. Используйте CALCULATE_FICO_FOR_CLIENT.");
        out.println("ERROR: Команда CALCULATE_FICO устарела. Используйте CALCULATE_FICO_FOR_CLIENT.");
    }

    // ======== Методы для клиентов и финансовой информации ========

    private void handleGetClients(PrintWriter out) {
        try {
            List<Client> clients = financialController.getAllClients();
            if (clients.isEmpty()) {
                logger.info("Список клиентов пуст");
                out.println("ERROR: Нет клиентов.");
                return;
            }
            StringBuilder response = new StringBuilder("OK");
            for (Client client : clients) {
                response.append("|").append(client.getId()).append(":").append(client.getFullName());
            }
            logger.info("Отправлен список клиентов: " + response);
            out.println(response.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении списка клиентов: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleGetFinancialInfo(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды GET_FINANCIAL_INFO: " + String.join(" ", parts));
            out.println("ERROR: GET_FINANCIAL_INFO client_id");
            return;
        }
        try {
            int clientId = Integer.parseInt(parts.get(1));
            FinancialInfo info = financialController.getFinancialInfo(clientId);
            if (info == null) {
                logger.warning("Финансовая информация для клиента с ID " + clientId + " не найдена");
                out.println("ERROR: Финансовая информация для клиента " + clientId + " не найдена");
                return;
            }
            String response = String.format(Locale.US, "OK|%d|%d|%.2f|%.2f|%d|%d|%s|%.2f|%s|%s",
                    info.getId(),
                    info.getClientId(),
                    info.getCreditBalance(),
                    info.getOverdueAmount(),
                    info.getOpenCredits(),
                    info.getCreditInquiries(),
                    info.getLastUpdated(),
                    info.getCreditLimit() != null ? info.getCreditLimit() : BigDecimal.ZERO,
                    info.getCreditTypes() != null ? info.getCreditTypes() : "",
                    info.getFirstCreditDate() != null ? info.getFirstCreditDate() : "");
            logger.info("Отправлена финансовая информация для клиента с ID " + clientId + ": " + response);
            out.println(response);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат client_id: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат client_id: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении финансовой информации: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleAddFinancialInfo(List<String> parts, PrintWriter out) {
        if (parts.size() != 9) {
            logger.warning("Неверный формат команды ADD_FINANCIAL_INFO: " + String.join(" ", parts));
            out.println("ERROR: ADD_FINANCIAL_INFO client_id credit_balance overdue_amount open_credits credit_inquiries credit_limit credit_types first_credit_date");
            return;
        }
        try {
            int clientId = Integer.parseInt(parts.get(1));
            BigDecimal creditBalance = new BigDecimal(parts.get(2).replace(',', '.'));
            BigDecimal overdueAmount = new BigDecimal(parts.get(3).replace(',', '.'));
            int openCredits = Integer.parseInt(parts.get(4));
            int creditInquiries = Integer.parseInt(parts.get(5));
            BigDecimal creditLimit = parts.get(6).equals("NULL") ? null : new BigDecimal(parts.get(6).replace(',', '.'));
            String creditTypes = parts.get(7).equals("NULL") ? null : parts.get(7);
            LocalDate firstCreditDate = parts.get(8).equals("NULL") ? null : LocalDate.parse(parts.get(8));

            if (!financialController.clientExists(clientId)) {
                logger.warning("Клиент с ID " + clientId + " не найден");
                out.println("ERROR: Клиент с ID " + clientId + " не найден");
                return;
            }

            String result = financialController.addFinancialInfo(clientId, creditBalance, overdueAmount,
                    openCredits, creditInquiries, LocalDate.now(), creditLimit, creditTypes, firstCreditDate);
            logger.info("Результат добавления финансовой информации для клиента с ID " + clientId + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат числа: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат числа: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении финансовой информации: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleUpdateFinancialInfo(List<String> parts, PrintWriter out) {
        if (parts.size() != 9) {
            logger.warning("Неверный формат команды UPDATE_FINANCIAL_INFO: " + String.join(" ", parts));
            out.println("ERROR: UPDATE_FINANCIAL_INFO id credit_balance overdue_amount open_credits credit_inquiries credit_limit credit_types first_credit_date");
            return;
        }
        try {
            int id = Integer.parseInt(parts.get(1));
            BigDecimal creditBalance = new BigDecimal(parts.get(2).replace(',', '.'));
            BigDecimal overdueAmount = new BigDecimal(parts.get(3).replace(',', '.'));
            int openCredits = Integer.parseInt(parts.get(4));
            int creditInquiries = Integer.parseInt(parts.get(5));
            BigDecimal creditLimit = parts.get(6).equals("NULL") ? null : new BigDecimal(parts.get(6).replace(',', '.'));
            String creditTypes = parts.get(7).equals("NULL") ? null : parts.get(7);
            LocalDate firstCreditDate = parts.get(8).equals("NULL") ? null : LocalDate.parse(parts.get(8));

            String result = financialController.updateFinancialInfo(id, creditBalance, overdueAmount,
                    openCredits, creditInquiries, LocalDate.now(), creditLimit, creditTypes, firstCreditDate);
            logger.info("Результат обновления финансовой информации с ID " + id + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат числа: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат числа: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении финансовой информации: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleDeleteFinancialInfo(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды DELETE_FINANCIAL_INFO: " + String.join(" ", parts));
            out.println("ERROR: DELETE_FINANCIAL_INFO id");
            return;
        }
        try {
            int id = Integer.parseInt(parts.get(1));
            String result = financialController.deleteFinancialInfo(id);
            logger.info("Результат удаления финансовой информации с ID " + id + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат ID: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат ID: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при удалении финансовой информации: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleGetClientFinancialInfo(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды GET_CLIENT_FINANCIAL_INFO: " + String.join(" ", parts));
            out.println("ERROR: GET_CLIENT_FINANCIAL_INFO client_id");
            return;
        }
        try {
            int clientId = Integer.parseInt(parts.get(1));
            FinancialInfo info = financialController.getFinancialInfo(clientId);
            if (info == null) {
                logger.warning("Финансовая информация для клиента с ID " + clientId + " не найдена");
                out.println("ERROR: Финансовая информация для клиента " + clientId + " не найдена");
                return;
            }
            String response = String.format(Locale.US, "OK|%d|%d|%.2f|%.2f|%d|%d|%s|%.2f|%s|%s",
                    info.getId(),
                    info.getClientId(),
                    info.getCreditBalance(),
                    info.getOverdueAmount(),
                    info.getOpenCredits(),
                    info.getCreditInquiries(),
                    info.getLastUpdated(),
                    info.getCreditLimit() != null ? info.getCreditLimit() : BigDecimal.ZERO,
                    info.getCreditTypes() != null ? info.getCreditTypes() : "",
                    info.getFirstCreditDate() != null ? info.getFirstCreditDate() : "");
            logger.info("Отправлена финансовая информация для клиента с ID " + clientId + ": " + response);
            out.println(response);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат client_id: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат client_id: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении финансовой информации клиента: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleCalculateFicoForClient(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды CALCULATE_FICO_FOR_CLIENT: " + String.join(" ", parts));
            out.println("ERROR: CALCULATE_FICO_FOR_CLIENT client_id");
            return;
        }
        try {
            int clientId = Integer.parseInt(parts.get(1));
            int ficoScore = financialController.calculateFicoScoreForClient(clientId);
            String creditApprovalStatus = financialController.getCreditApprovalStatus(clientId, ficoScore);
            String response = String.format("OK:%d:%d:%s", clientId, ficoScore, creditApprovalStatus);
            logger.info("Результат расчёта FICO для клиента с ID " + clientId + ": " + response);
            out.println(response);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат client_id: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат client_id: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при расчёте FICO-балла: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    // ======== Методы для кредитных заявок ========

    private void handleGetAllCreditApplications(PrintWriter out) {
        try {
            List<CreditApplication> applications = creditApplicationController.getAllApplications();
            if (applications.isEmpty()) {
                logger.info("Список кредитных заявок пуст");
                out.println("ERROR: Заявки не найдены");
                return;
            }
            StringBuilder response = new StringBuilder("OK");
            for (CreditApplication app : applications) {
                String appData = String.format(Locale.US, "|%d,%d,%.2f,%d,%s,%s,%s",
                        app.getId(),
                        app.getClientId(),
                        app.getLoanAmount(),
                        app.getLoanTerm(),
                        app.getPurpose(),
                        app.getStatus(),
                        app.getSubmissionDate());
                response.append(appData);
            }
            logger.info("Отправлен список всех кредитных заявок: " + response);
            out.println(response.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении списка заявок: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleGetCreditApplications(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды GET_CREDIT_APPLICATIONS: " + String.join(" ", parts));
            out.println("ERROR: GET_CREDIT_APPLICATIONS client_id");
            return;
        }
        try {
            int clientId = Integer.parseInt(parts.get(1));
            List<CreditApplication> applications = creditApplicationController.getApplicationsByClientId(clientId);
            if (applications.isEmpty()) {
                logger.info("Кредитные заявки для клиента с ID " + clientId + " не найдены");
                out.println("ERROR: Заявки для клиента " + clientId + " не найдены");
                return;
            }
            StringBuilder response = new StringBuilder("OK");
            for (CreditApplication app : applications) {
                String appData = String.format(Locale.US, "|%d,%d,%.2f,%d,%s,%s,%s",
                        app.getId(),
                        app.getClientId(),
                        app.getLoanAmount(),
                        app.getLoanTerm(),
                        app.getPurpose(),
                        app.getStatus(),
                        app.getSubmissionDate());
                response.append(appData);
            }
            logger.info("Отправлены кредитные заявки для клиента с ID " + clientId + ": " + response);
            out.println(response.toString());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат client_id: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат client_id: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении заявок клиента: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleAddCreditApplication(List<String> parts, PrintWriter out) {
        if (parts.size() != 5) {
            logger.warning("Неверный формат команды ADD_CREDIT_APPLICATION: " + String.join(" ", parts));
            out.println("ERROR: ADD_CREDIT_APPLICATION client_id loan_amount loan_term purpose");
            return;
        }
        try {
            int clientId = Integer.parseInt(parts.get(1));
            BigDecimal loanAmount = new BigDecimal(parts.get(2).replace(',', '.'));
            int loanTerm = Integer.parseInt(parts.get(3));
            String purpose = parts.get(4);

            if (!creditApplicationController.clientExists(clientId)) {
                logger.warning("Клиент с ID " + clientId + " не найден");
                out.println("ERROR: Клиент с ID " + clientId + " не найден");
                return;
            }

            String result = creditApplicationController.addCreditApplication(clientId, loanAmount, loanTerm, purpose, LocalDate.now());
            logger.info("Результат добавления кредитной заявки для клиента с ID " + clientId + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат числа: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат числа: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении кредитной заявки: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleUpdateCreditApplicationStatus(List<String> parts, PrintWriter out) {
        if (parts.size() != 3) {
            logger.warning("Неверный формат команды UPDATE_CREDIT_APPLICATION_STATUS: " + String.join(" ", parts));
            out.println("ERROR: UPDATE_CREDIT_APPLICATION_STATUS id status");
            return;
        }
        try {
            int id = Integer.parseInt(parts.get(1));
            String status = parts.get(2).toUpperCase();
            if (!status.equals("PENDING") && !status.equals("APPROVED") && !status.equals("REJECTED")) {
                logger.warning("Неверный статус кредитной заявки: " + status);
                out.println("ERROR: Неверный статус. Ожидается: PENDING, APPROVED, REJECTED");
                return;
            }
            String result = creditApplicationController.updateApplicationStatus(id, status);
            logger.info("Результат обновления статуса заявки с ID " + id + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат ID: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат ID: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении статуса заявки: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleDeleteCreditApplication(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды DELETE_CREDIT_APPLICATION: " + String.join(" ", parts));
            out.println("ERROR: DELETE_CREDIT_APPLICATION id");
            return;
        }
        try {
            int id = Integer.parseInt(parts.get(1));
            String result = creditApplicationController.deleteCreditApplication(id);
            logger.info("Результат удаления кредитной заявки с ID " + id + ": " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат ID: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат ID: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при удалении кредитной заявки: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    // ======== Методы для управления логами безопасности ========

    private void handleGetSecurityLogs(List<String> parts, PrintWriter out) {
        String userFilter = null;
        String actionFilter = null;

        // Парсинг фильтров
        for (int i = 1; i < parts.size(); i += 2) {
            if (i + 1 >= parts.size()) break;
            String filterType = parts.get(i);
            String filterValue = parts.get(i + 1);
            if (filterType.equals("USER_FILTER")) {
                userFilter = filterValue;
            } else if (filterType.equals("ACTION_FILTER")) {
                actionFilter = filterValue;
            }
        }

        try {
            List<SecurityLog> logs = securityLogsController.getSecurityLogs(userFilter, actionFilter);
            if (logs.isEmpty()) {
                logger.info("Логи не найдены для фильтров: user=" + userFilter + ", action=" + actionFilter);
                out.println("OK");
                return;
            }

            StringBuilder response = new StringBuilder("OK");
            for (SecurityLog log : logs) {
                response.append("|").append(log.toString());
            }
            logger.info("Отправлены логи безопасности: " + response);
            out.println(response.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при получении логов: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void handleClearOldLogs(List<String> parts, PrintWriter out) {
        if (parts.size() != 2) {
            logger.warning("Неверный формат команды CLEAR_OLD_LOGS: " + String.join(" ", parts));
            out.println("ERROR: CLEAR_OLD_LOGS days");
            return;
        }
        try {
            int days = Integer.parseInt(parts.get(1));
            if (days <= 0) {
                logger.warning("Количество дней должно быть положительным: " + days);
                out.println("ERROR: Количество дней должно быть положительным");
                return;
            }
            String result = securityLogsController.clearOldLogs(days);
            logger.info("Результат очистки старых логов: " + result);
            out.println(result);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Неверный формат количества дней: " + e.getMessage(), e);
            out.println("ERROR: Неверный формат количества дней: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при очистке логов: " + e.getMessage(), e);
            out.println("ERROR: " + e.getMessage());
        }
    }
}