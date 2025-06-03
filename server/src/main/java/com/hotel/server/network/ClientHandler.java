package com.hotel.server.network;

import com.hotel.common.*;
import com.hotel.server.service.AuthManager;
import com.hotel.server.service.BookingService;
import com.hotel.server.service.ReviewService;
import com.hotel.server.service.RoomService;
import com.hotel.server.service.UserService;
import com.hotel.server.handler.FetchRoomImagesHandler;
import com.hotel.server.handler.AddRoomImageHandler;
import com.hotel.server.handler.DeleteRoomImageHandler;
import com.hotel.server.handler.FetchPartnersHandler;
import com.hotel.server.handler.CreatePartnerHandler;
import com.hotel.server.handler.UpdatePartnerHandler;
import com.hotel.server.handler.DeletePartnerHandler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.time.LocalDate;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Consumer<String> logger;

    public ClientHandler(Socket socket, Consumer<String> logger) {
        this.socket = socket;
        this.logger = logger;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())
        ) {
            Object obj = in.readObject();

            if (obj instanceof RegisterUserRequest) {
                RegisterUserRequest req = (RegisterUserRequest) obj;
                RegisterUserResponse resp = AuthManager.getInstance().registerUser(req);
                out.writeObject(resp);
                out.flush();
                logger.accept("RegisterUser: " + (resp.isSuccess() ? "OK" : resp.getMessage()));
                return;
            }

            if (obj instanceof LoginRequest) {
                LoginRequest req = (LoginRequest) obj;
                logger.accept("Login attempt: " + req.getUsername());
                LoginResponse resp = AuthManager.getInstance()
                        .authenticate(req.getUsername(), req.getPassword());
                out.writeObject(resp);
                out.flush();
                logger.accept("Sent response: success=" + resp.isSuccess());
                return;
            }

            if (obj instanceof FetchRoomsRequest) {
                logger.accept("FetchRoomsRequest received");
                List<RoomDTO> rooms = new RoomService().getAllRooms();
                // поправьте конструктор, чтобы принимать success
                FetchRoomsResponse resp = new FetchRoomsResponse(rooms, "OK", true);
                out.writeObject(resp);
                out.flush();
                logger.accept("Sent rooms list: count=" + rooms.size());
                return;
            }

            if (obj instanceof FetchBookingsRequest) {
                logger.accept("FetchBookingsRequest received");
                List<BookingDTO> bookings = new BookingService().getAllBookings();
                FetchBookingsResponse resp = new FetchBookingsResponse(bookings, "OK", true);
                out.writeObject(resp);
                out.flush();
                logger.accept("Sent bookings list: count=" + bookings.size());
                return;
            }

            if (obj instanceof CreateBookingRequest) {
                try {
                    CreateBookingRequest req = (CreateBookingRequest) obj;
                    logger.accept("CreateBookingRequest received: UserID=" + req.getUserId() + 
                                 ", RoomID=" + req.getRoomId());
                                 
                    boolean ok = new BookingService().createBookingFromRequest(req);
                    CreateBookingResponse resp = new CreateBookingResponse(
                        ok, 
                        ok ? "Бронирование успешно создано" : "Не удалось создать бронирование"
                    );
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("CreateBooking: " + (ok ? "success" : "fail"));
                } catch (Exception e) {
                    logger.accept("Error processing CreateBookingRequest: " + e.getMessage());
                    CreateBookingResponse resp = new CreateBookingResponse(false, 
                        "Ошибка обработки запроса: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }

            if (obj instanceof DeleteRoomRequest) {
                try {
                    DeleteRoomRequest req = (DeleteRoomRequest) obj;
                    int roomId = req.getRoomId();
                    logger.accept("DeleteRoomRequest received: RoomID=" + roomId);
                    
                    boolean success = new RoomService().deleteRoom(roomId);
                    DeleteRoomResponse resp = new DeleteRoomResponse(
                        success,
                        success ? null : "Не удалось удалить номер. Возможно, он связан с бронированиями."
                    );
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("DeleteRoom: " + (success ? "success" : "fail") + " for roomId=" + roomId);
                } catch (Exception e) {
                    logger.accept("Error processing DeleteRoomRequest: " + e.getMessage());
                    DeleteRoomResponse resp = new DeleteRoomResponse(false, 
                        "Ошибка обработки запроса: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            if (obj instanceof UpdateRoomRequest) {
                try {
                    UpdateRoomRequest req = (UpdateRoomRequest) obj;
                    logger.accept("UpdateRoomRequest received: " + (req.isNewRoom() ? "New room" : "Update room ID=" + req.getRoomId()));
                    
                    RoomDTO updatedRoom = new RoomService().updateRoom(req);
                    UpdateRoomResponse resp;
                    
                    if (updatedRoom != null) {
                        resp = new UpdateRoomResponse(true, updatedRoom);
                        logger.accept("UpdateRoom: success");
                    } else {
                        resp = new UpdateRoomResponse(false, "Не удалось обновить/создать номер");
                        logger.accept("UpdateRoom: fail");
                    }
                    
                    out.writeObject(resp);
                    out.flush();
                } catch (Exception e) {
                    logger.accept("Error processing UpdateRoomRequest: " + e.getMessage());
                    UpdateRoomResponse resp = new UpdateRoomResponse(false, 
                        "Ошибка обработки запроса: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }

            // Обработка запроса на получение всех бронирований
            if (obj instanceof GetAllBookingsRequest) {
                try {
                    logger.accept("GetAllBookingsRequest received");
                    
                    List<BookingDTO> bookings = new BookingService().getAllBookings();
                    GetAllBookingsResponse resp = new GetAllBookingsResponse(bookings);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("Sent all bookings: count=" + bookings.size());
                } catch (Exception e) {
                    logger.accept("Error processing GetAllBookingsRequest: " + e.getMessage());
                    GetAllBookingsResponse resp = new GetAllBookingsResponse("Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на обновление статуса бронирования
            if (obj instanceof UpdateBookingStatusRequest) {
                try {
                    UpdateBookingStatusRequest req = (UpdateBookingStatusRequest) obj;
                    int bookingId = req.getBookingId();
                    String newStatus = req.getNewStatus();
                    
                    logger.accept("UpdateBookingStatusRequest received: BookingID=" + bookingId + 
                                 ", NewStatus=" + newStatus);
                    
                    Optional<BookingDTO> updatedBookingOpt = new BookingService()
                            .updateBookingStatus(bookingId, newStatus);
                    
                    UpdateBookingStatusResponse resp;
                    if (updatedBookingOpt.isPresent()) {
                        resp = new UpdateBookingStatusResponse(updatedBookingOpt.get());
                        logger.accept("UpdateBookingStatus: success");
                    } else {
                        resp = new UpdateBookingStatusResponse(false, 
                                "Не удалось обновить статус бронирования");
                        logger.accept("UpdateBookingStatus: fail");
                    }
                    
                    out.writeObject(resp);
                    out.flush();
                } catch (Exception e) {
                    logger.accept("Error processing UpdateBookingStatusRequest: " + e.getMessage());
                    UpdateBookingStatusResponse resp = new UpdateBookingStatusResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на получение бронирований гостя
            if (obj instanceof GetGuestBookingsRequest) {
                try {
                    GetGuestBookingsRequest req = (GetGuestBookingsRequest) obj;
                    int userId = req.getUserId();
                    String statusFilter = req.getStatusFilter();
                    LocalDate startDateFilter = req.getStartDateFilter();
                    
                    logger.accept("GetGuestBookingsRequest received: UserID=" + userId + 
                                 (statusFilter != null ? ", Status=" + statusFilter : "") +
                                 (startDateFilter != null ? ", Since=" + startDateFilter : ""));
                    
                    List<BookingDTO> bookings = new BookingService()
                            .getBookingsForUser(userId, statusFilter, startDateFilter);
                    
                    GetGuestBookingsResponse resp = new GetGuestBookingsResponse(bookings);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("Sent guest bookings: count=" + bookings.size());
                } catch (Exception e) {
                    logger.accept("Error processing GetGuestBookingsRequest: " + e.getMessage());
                    GetGuestBookingsResponse resp = new GetGuestBookingsResponse("Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на отмену бронирования
            if (obj instanceof CancelBookingRequest) {
                try {
                    CancelBookingRequest req = (CancelBookingRequest) obj;
                    int bookingId = req.getBookingId();
                    int userId = req.getUserId();
                    String reason = req.getReason();
                    
                    logger.accept("CancelBookingRequest received: BookingID=" + bookingId + 
                                 ", UserID=" + userId);
                    
                    Optional<BookingDTO> cancelledBookingOpt = new BookingService()
                            .cancelBooking(bookingId, userId, reason);
                    
                    CancelBookingResponse resp;
                    if (cancelledBookingOpt.isPresent()) {
                        resp = new CancelBookingResponse(true, 
                                "Бронирование успешно отменено", 
                                cancelledBookingOpt.get());
                        logger.accept("CancelBooking: success");
                    } else {
                        resp = new CancelBookingResponse(false, 
                                "Не удалось отменить бронирование. Проверьте права доступа или статус бронирования.");
                        logger.accept("CancelBooking: fail");
                    }
                    
                    out.writeObject(resp);
                    out.flush();
                } catch (Exception e) {
                    logger.accept("Error processing CancelBookingRequest: " + e.getMessage());
                    CancelBookingResponse resp = new CancelBookingResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на создание отзыва
            if (obj instanceof CreateReviewRequest) {
                try {
                    CreateReviewRequest req = (CreateReviewRequest) obj;
                    int userId = req.getUserId();
                    int roomId = req.getRoomId();
                    Integer bookingId = req.getBookingId();
                    int rating = req.getRating();
                    String comment = req.getComment();
                    
                    logger.accept("CreateReviewRequest received: UserID=" + userId + 
                                 ", RoomID=" + roomId + 
                                 ", BookingID=" + bookingId + 
                                 ", Rating=" + rating);
                    
                    boolean success = new ReviewService().createReview(userId, roomId, bookingId, rating, comment);
                    
                    CreateReviewResponse resp;
                    if (success) {
                        resp = new CreateReviewResponse(true, "Отзыв успешно добавлен");
                        logger.accept("CreateReview: success");
                    } else {
                        resp = new CreateReviewResponse(false, 
                                "Не удалось добавить отзыв. Проверьте данные или возможно отзыв уже был оставлен.");
                        logger.accept("CreateReview: fail");
                    }
                    
                    out.writeObject(resp);
                    out.flush();
                } catch (Exception e) {
                    logger.accept("Error processing CreateReviewRequest: " + e.getMessage());
                    e.printStackTrace();
                    CreateReviewResponse resp = new CreateReviewResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на получение отзывов
            if (obj instanceof FetchReviewsRequest) {
                try {
                    FetchReviewsRequest req = (FetchReviewsRequest) obj;
                    List<ReviewDTO> reviews;
                    
                    if (req.getFetchAll() != null && req.getFetchAll()) {
                        logger.accept("FetchReviewsRequest received: Fetching all reviews");
                        reviews = new ReviewService().getAllReviews();
                    } else if (req.getUserId() != null) {
                        logger.accept("FetchReviewsRequest received: Fetching reviews for UserID=" + req.getUserId());
                        reviews = new ReviewService().getReviewsByUserId(req.getUserId());
                    } else if (req.getRoomId() != null) {
                        logger.accept("FetchReviewsRequest received: Fetching reviews for RoomID=" + req.getRoomId());
                        reviews = new ReviewService().getReviewsByRoomId(req.getRoomId());
                    } else {
                        logger.accept("FetchReviewsRequest received: Invalid request parameters");
                        reviews = List.of();
                    }
                    
                    FetchReviewsResponse resp = new FetchReviewsResponse(true, 
                            "Отзывы успешно получены", reviews);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("Sent reviews: count=" + reviews.size());
                } catch (Exception e) {
                    logger.accept("Error processing FetchReviewsRequest: " + e.getMessage());
                    FetchReviewsResponse resp = new FetchReviewsResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на удаление отзыва
            if (obj instanceof DeleteReviewRequest) {
                try {
                    DeleteReviewRequest req = (DeleteReviewRequest) obj;
                    int reviewId = req.getReviewId();
                    int userId = req.getUserId();
                    
                    logger.accept("DeleteReviewRequest received: ReviewID=" + reviewId + 
                                 ", UserID=" + userId);
                    
                    // Проверка прав доступа (администратор или автор отзыва)
                    // Предполагаем, что пользователи с ID 1 (администратор) и 2 (директор) имеют права администратора
                    boolean isAdmin = (userId == 1 || userId == 2); 
                    boolean success = new ReviewService().deleteReview(reviewId, userId, isAdmin);
                    
                    DeleteReviewResponse resp;
                    if (success) {
                        resp = new DeleteReviewResponse(true, "Отзыв успешно удален");
                        logger.accept("DeleteReview: success");
                    } else {
                        resp = new DeleteReviewResponse(false, 
                                "Не удалось удалить отзыв. Проверьте права доступа.");
                        logger.accept("DeleteReview: fail");
                    }
                    
                    out.writeObject(resp);
                    out.flush();
                } catch (Exception e) {
                    logger.accept("Error processing DeleteReviewRequest: " + e.getMessage());
                    DeleteReviewResponse resp = new DeleteReviewResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на создание тестовых отзывов
            if (obj instanceof CreateTestReviewsRequest) {
                try {
                    CreateTestReviewsRequest req = (CreateTestReviewsRequest) obj;
                    Integer count = req.getCount();
                    
                    if (count == null || count <= 0) {
                        count = 10; // По умолчанию создаем 10 тестовых отзывов
                    }
                    
                    logger.accept("CreateTestReviewsRequest received: Count=" + count);
                    
                    List<ReviewDTO> createdReviews = new ReviewService().createTestReviews(count);
                    
                    CreateTestReviewsResponse resp = new CreateTestReviewsResponse(
                            !createdReviews.isEmpty(),
                            createdReviews.isEmpty() ? "Не удалось создать тестовые отзывы" : 
                                    "Создано " + createdReviews.size() + " тестовых отзывов",
                            createdReviews
                    );
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("CreateTestReviews: " + 
                                 (createdReviews.isEmpty() ? "fail" : "created " + createdReviews.size()));
                } catch (Exception e) {
                    logger.accept("Error processing CreateTestReviewsRequest: " + e.getMessage());
                    e.printStackTrace();
                    CreateTestReviewsResponse resp = new CreateTestReviewsResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на получение списка пользователей
            if (obj instanceof FetchUsersRequest) {
                try {
                    FetchUsersRequest req = (FetchUsersRequest) obj;
                    String roleFilter = req.getRoleFilter();
                    
                    logger.accept("FetchUsersRequest received" + 
                                 (roleFilter != null ? " with role filter: " + roleFilter : ""));
                    
                    List<UserDTO> users;
                    if (roleFilter != null && !roleFilter.isEmpty()) {
                        users = new UserService().getUsersByRole(roleFilter);
                    } else {
                        users = new UserService().getAllUsers();
                    }
                    
                    FetchUsersResponse resp = new FetchUsersResponse(true, 
                            "Пользователи успешно получены", users);
                    
                    logger.accept("Sending response with " + users.size() + " users");
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("Sent users: count=" + users.size());
                } catch (Exception e) {
                    logger.accept("Error processing FetchUsersRequest: " + e.getMessage());
                    FetchUsersResponse resp = new FetchUsersResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на получение изображений комнаты
            if (obj instanceof FetchRoomImagesRequest) {
                try {
                    FetchRoomImagesRequest req = (FetchRoomImagesRequest) obj;
                    logger.accept("FetchRoomImagesRequest received: RoomID=" + req.getRoomId());
                    
                    FetchRoomImagesResponse resp = new FetchRoomImagesHandler().handle(req);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("FetchRoomImages: " + (resp.isSuccess() ? "success" : "fail") + 
                                 (resp.getImages() != null ? ", images count=" + resp.getImages().size() : ""));
                } catch (Exception e) {
                    logger.accept("Error processing FetchRoomImagesRequest: " + e.getMessage());
                    FetchRoomImagesResponse resp = new FetchRoomImagesResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на добавление изображения комнаты
            if (obj instanceof AddRoomImageRequest) {
                try {
                    AddRoomImageRequest req = (AddRoomImageRequest) obj;
                    logger.accept("AddRoomImageRequest received: RoomID=" + req.getRoomId() + 
                                 ", FileName=" + req.getFileName());
                    
                    AddRoomImageResponse resp = new AddRoomImageHandler().handle(req);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("AddRoomImage: " + (resp.isSuccess() ? "success" : "fail"));
                } catch (Exception e) {
                    logger.accept("Error processing AddRoomImageRequest: " + e.getMessage());
                    AddRoomImageResponse resp = new AddRoomImageResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на удаление изображения комнаты
            if (obj instanceof DeleteRoomImageRequest) {
                try {
                    DeleteRoomImageRequest req = (DeleteRoomImageRequest) obj;
                    logger.accept("DeleteRoomImageRequest received: ImageID=" + req.getImageId());
                    
                    DeleteRoomImageResponse resp = new DeleteRoomImageHandler().handle(req);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("DeleteRoomImage: " + (resp.isSuccess() ? "success" : "fail"));
                } catch (Exception e) {
                    logger.accept("Error processing DeleteRoomImageRequest: " + e.getMessage());
                    DeleteRoomImageResponse resp = new DeleteRoomImageResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на получение списка партнеров
            if (obj instanceof FetchPartnersRequest) {
                try {
                    FetchPartnersRequest req = (FetchPartnersRequest) obj;
                    logger.accept("FetchPartnersRequest received");
                    
                    FetchPartnersResponse resp = new FetchPartnersHandler().handle(req);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("FetchPartners: " + (resp.isSuccess() ? "success" : "fail") + 
                                 (resp.getPartners() != null ? ", partners count=" + resp.getPartners().size() : ""));
                } catch (Exception e) {
                    logger.accept("Error processing FetchPartnersRequest: " + e.getMessage());
                    FetchPartnersResponse resp = new FetchPartnersResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на создание нового партнера
            if (obj instanceof CreatePartnerRequest) {
                try {
                    CreatePartnerRequest req = (CreatePartnerRequest) obj;
                    logger.accept("CreatePartnerRequest received: Name=" + req.getName());
                    
                    CreatePartnerResponse resp = new CreatePartnerHandler().handle(req);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("CreatePartner: " + (resp.isSuccess() ? "success" : "fail"));
                } catch (Exception e) {
                    logger.accept("Error processing CreatePartnerRequest: " + e.getMessage());
                    CreatePartnerResponse resp = new CreatePartnerResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на обновление информации о партнере
            if (obj instanceof UpdatePartnerRequest) {
                try {
                    UpdatePartnerRequest req = (UpdatePartnerRequest) obj;
                    logger.accept("UpdatePartnerRequest received: PartnerID=" + req.getPartnerId());
                    
                    UpdatePartnerResponse resp = new UpdatePartnerHandler().handle(req);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("UpdatePartner: " + (resp.isSuccess() ? "success" : "fail"));
                } catch (Exception e) {
                    logger.accept("Error processing UpdatePartnerRequest: " + e.getMessage());
                    UpdatePartnerResponse resp = new UpdatePartnerResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Обработка запроса на удаление партнера
            if (obj instanceof DeletePartnerRequest) {
                try {
                    DeletePartnerRequest req = (DeletePartnerRequest) obj;
                    logger.accept("DeletePartnerRequest received: PartnerID=" + req.getPartnerId());
                    
                    DeletePartnerResponse resp = new DeletePartnerHandler().handle(req);
                    
                    out.writeObject(resp);
                    out.flush();
                    logger.accept("DeletePartner: " + (resp.isSuccess() ? "success" : "fail"));
                } catch (Exception e) {
                    logger.accept("Error processing DeletePartnerRequest: " + e.getMessage());
                    DeletePartnerResponse resp = new DeletePartnerResponse(false, 
                            "Ошибка: " + e.getMessage());
                    out.writeObject(resp);
                    out.flush();
                }
                return;
            }
            
            // Если неожиданный объект
            logger.accept("Unknown request type: " + obj.getClass().getSimpleName());
            
        } catch (Exception e) {
            logger.accept("Client handler error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

}