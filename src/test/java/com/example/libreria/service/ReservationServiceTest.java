package com.example.libreria.service;

import com.example.libreria.dto.ReservationRequestDTO;
import com.example.libreria.dto.ReservationResponseDTO;
import com.example.libreria.dto.ReturnBookRequestDTO;
import com.example.libreria.model.Book;
import com.example.libreria.model.Reservation;
import com.example.libreria.model.User;
import com.example.libreria.repository.BookRepository;
import com.example.libreria.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private BookService bookService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private ReservationService reservationService;
    
    private User testUser;
    private Book testBook;
    private Reservation testReservation;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Juan Pérez");
        testUser.setEmail("juan@example.com");
        
        testBook = new Book();
        testBook.setExternalId(258027L);
        testBook.setTitle("The Lord of the Rings");
        testBook.setPrice(new BigDecimal("15.99"));
        testBook.setStockQuantity(10);
        testBook.setAvailableQuantity(5);
        
        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setUser(testUser);
        testReservation.setBook(testBook);
        testReservation.setRentalDays(7);
        testReservation.setStartDate(LocalDate.now());
        testReservation.setExpectedReturnDate(LocalDate.now().plusDays(7));
        testReservation.setDailyRate(new BigDecimal("15.99"));
        testReservation.setTotalFee(new BigDecimal("111.93"));
        testReservation.setStatus(Reservation.ReservationStatus.ACTIVE);
        testReservation.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    void testCreateReservation_Success() {
        // TODO: Implementar el test de creación de reserva exitosa LISTO
        ReservationRequestDTO request = new ReservationRequestDTO(
                1L,
                258027L,
                7,
                LocalDate.now()
        );

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(bookRepository.findByExternalId(258027L)).thenReturn(Optional.of(testBook));

        Reservation saved = new Reservation();
        saved.setId(99L);
        saved.setUser(testUser);
        saved.setBook(testBook);

        when(reservationRepository.save(any())).thenReturn(saved);

        ReservationResponseDTO result = reservationService.createReservation(request);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        verify(bookService).decreaseAvailableQuantity(258027L);
        verify(reservationRepository).save(any(Reservation.class));
    }
    
    @Test
    void testCreateReservation_BookNotAvailable() {
        // TODO: Implementar el test de creación de reserva cuando el libro no está disponible LISTO
        ReservationRequestDTO request = new ReservationRequestDTO(
                1L,
                258027L,
                7,
                LocalDate.now()
        );

        testBook.setAvailableQuantity(0);

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(bookRepository.findByExternalId(258027L)).thenReturn(Optional.of(testBook));

        doThrow(new RuntimeException("No hay libros disponibles"))
                .when(bookService).decreaseAvailableQuantity(258027L);

        assertThrows(RuntimeException.class, () -> reservationService.createReservation(request));
    }
    
    @Test
    void testReturnBook_OnTime() {
        // TODO: Implementar el test de devolución de libro en tiempo  LISTO
        testReservation.setExpectedReturnDate(LocalDate.now());
        testReservation.setActualReturnDate(null);
        testReservation.setStatus(Reservation.ReservationStatus.ACTIVE);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any())).thenReturn(testReservation);

        ReturnBookRequestDTO dto = new ReturnBookRequestDTO(LocalDate.now());

        ReservationResponseDTO result = reservationService.returnBook(1L, dto);

        assertEquals(Reservation.ReservationStatus.RETURNED, result.getStatus());
        verify(bookService).increaseAvailableQuantity(testBook.getExternalId());
    }
    
    @Test
    void testReturnBook_Overdue() {
        // TODO: Implementar el test de devolución de libro con retraso LISTO
        testReservation.setExpectedReturnDate(LocalDate.now().minusDays(3));
        testReservation.setActualReturnDate(null);
        testReservation.setStatus(Reservation.ReservationStatus.ACTIVE);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any())).thenReturn(testReservation);

        ReturnBookRequestDTO dto = new ReturnBookRequestDTO(LocalDate.now());

        ReservationResponseDTO result = reservationService.returnBook(1L, dto);

        assertEquals(Reservation.ReservationStatus.OVERDUE, result.getStatus());
        assertTrue(result.getLateFee().compareTo(BigDecimal.ZERO) > 0);

        verify(bookService).increaseAvailableQuantity(testBook.getExternalId());
    }
    
    @Test
    void testGetReservationById_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        
        ReservationResponseDTO result = reservationService.getReservationById(1L);
        
        assertNotNull(result);
        assertEquals(testReservation.getId(), result.getId());
    }
    
    @Test
    void testGetAllReservations() {
        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(testReservation, reservation2));
        
        List<ReservationResponseDTO> result = reservationService.getAllReservations();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    @Test
    void testGetReservationsByUserId() {
        when(reservationRepository.findByUserId(1L)).thenReturn(Arrays.asList(testReservation));
        
        List<ReservationResponseDTO> result = reservationService.getReservationsByUserId(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    void testGetActiveReservations() {
        when(reservationRepository.findByStatus(Reservation.ReservationStatus.ACTIVE))
                .thenReturn(Arrays.asList(testReservation));
        
        List<ReservationResponseDTO> result = reservationService.getActiveReservations();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

