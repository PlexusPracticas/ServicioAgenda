package org.example.servicioagenda.service;

import org.example.servicioagenda.dto.request.AssignedDeviceInputDTO;
import org.example.servicioagenda.dto.request.DeviceAdd;
import org.example.servicioagenda.dto.request.EmployeeInputDTO;
import org.example.servicioagenda.dto.request.UpdateAssignedToRequest;
import org.example.servicioagenda.dto.response.*;
import org.example.servicioagenda.mapper.AgendaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListContactsServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AgendaMapper mapper;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private ListContactsServiceImpl service;


    @Test
    void listEmployees_emptyAgenda() {

        Mockito.doNothing()
                .when(tokenService)
                .decrypt(Mockito.anyString());

        EmployeeServiceResponse external = new EmployeeServiceResponse();
        external.setEmployees(List.of());
        external.setTotalElements(0);
        external.setTotalPages(0);

        Mockito.when(restTemplate.exchange(
                Mockito.contains("http://localhost:8080/employees"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(HttpEntity.class),
                Mockito.eq(EmployeeServiceResponse.class)
        )).thenReturn(ResponseEntity.ok(external));

        AgendaResponse response = service.listEmployees("token", 0, 10);

        assertNotNull(response);
        assertNotNull(response.getEmployee());
        assertTrue(response.getEmployee().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
    }

    @Test
    void listEmployees_deviceNotFound_setsNull() {

        Mockito.doNothing().when(tokenService).decrypt(anyString());

        EmployeeExternalDTO empExternal = new EmployeeExternalDTO();
        empExternal.setId(1);

        EmployeeServiceResponse external = new EmployeeServiceResponse();
        external.setEmployees(List.of(empExternal));
        external.setTotalElements(1);
        external.setTotalPages(1);

        EmployeeAgendaDTO agendaDTO = new EmployeeAgendaDTO();
        agendaDTO.setEmployeeId(1);

        Mockito.when(restTemplate.exchange(
                contains("http://localhost:8080/employees"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeServiceResponse.class)
        )).thenReturn(ResponseEntity.ok(external));

        Mockito.when(mapper.toAgenda(empExternal))
                .thenReturn(agendaDTO);

        Mockito.when(restTemplate.exchange(
                contains("http://localhost:8081/devices/assignation/1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        )).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        AgendaResponse response = service.listEmployees("token", 0, 10);

        assertEquals(1, response.getEmployee().size());
        assertNull(response.getEmployee().get(0).getAssignedDevice());
    }

    @Test
    void listEmployees_deviceAssigned_ok() {

        doNothing().when(tokenService).decrypt(anyString());

        EmployeeExternalDTO empExternal = new EmployeeExternalDTO();
        empExternal.setId(1);

        EmployeeServiceResponse external = new EmployeeServiceResponse();
        external.setEmployees(List.of(empExternal));
        external.setTotalElements(1);
        external.setTotalPages(1);

        EmployeeAgendaDTO agendaDTO = new EmployeeAgendaDTO();
        agendaDTO.setEmployeeId(1);

        DeviceDTO device = new DeviceDTO();
        device.setAssignedTo(1);

        when(restTemplate.exchange(
                contains("http://localhost:8080/employees"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeServiceResponse.class)
        )).thenReturn(ResponseEntity.ok(external));

        when(mapper.toAgenda(empExternal)).thenReturn(agendaDTO);

        when(restTemplate.exchange(
                contains("http://localhost:8081/devices/assignation/1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        )).thenReturn(ResponseEntity.ok(device));

        AgendaResponse response = service.listEmployees("token", 0, 10);

        assertEquals(1, response.getEmployee().size());
        assertNotNull(response.getEmployee().get(0).getAssignedDevice());
        assertEquals(1,
                response.getEmployee().get(0).getAssignedDevice().getAssignedTo());
    }


    @Test
    void filterEmployees_emptyAgenda() {

        doNothing().when(tokenService).decrypt(anyString());

        EmployeeServiceResponse external = new EmployeeServiceResponse();
        external.setEmployees(List.of());
        external.setTotalElements(0);
        external.setTotalPages(0);

        when(restTemplate.exchange(
                contains("http://localhost:8080/employees/search"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EmployeeServiceResponse.class)
        )).thenReturn(ResponseEntity.ok(external));

        AgendaResponse response =
                service.filterEmployees("token", "Juan", "name", 0, 10);

        assertNotNull(response);
        assertTrue(response.getEmployee().isEmpty());
    }


    @Test
    void filterEmployees_ok_withDevice() {
        doNothing().when(tokenService).decrypt(anyString());
        EmployeeExternalDTO empExternal = new EmployeeExternalDTO();
        empExternal.setId(2);
        EmployeeServiceResponse external = new EmployeeServiceResponse();
        external.setEmployees(List.of(empExternal));
        external.setTotalElements(1);
        external.setTotalPages(1);
        EmployeeAgendaDTO agendaDTO = new EmployeeAgendaDTO();
        agendaDTO.setEmployeeId(2);
        DeviceDTO device = new DeviceDTO();
        device.setAssignedTo(2);
        when(restTemplate.exchange(contains("employees/search"), eq(HttpMethod.GET), any(HttpEntity.class), eq(EmployeeServiceResponse.class))).thenReturn(ResponseEntity.ok(external));
        when(mapper.toAgenda(empExternal)).thenReturn(agendaDTO);
        when(restTemplate.exchange(contains("devices/assignation/2"), eq(HttpMethod.GET), any(HttpEntity.class), eq(DeviceDTO.class))).thenReturn(ResponseEntity.ok(device));
        AgendaResponse response = service.filterEmployees("token", "Juan", "name", 0, 10);
        assertEquals(1, response.getEmployee().size());
        assertNotNull(response.getEmployee().get(0).getAssignedDevice());
        assertEquals(2, response.getEmployee().get(0).getAssignedDevice().getAssignedTo());
    }


    @Test
    void createEmployee_ok() {

        doNothing().when(tokenService).decrypt(anyString());

        EmployeeCreateResponse created = new EmployeeCreateResponse();
        created.setId(10);

        when(restTemplate.exchange(
                eq("http://localhost:8080/employees"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(EmployeeCreateResponse[].class)
        )).thenReturn(ResponseEntity.ok(new EmployeeCreateResponse[]{created}));

        EmployeeCreateResponse response =
                service.createEmployee("token", new EmployeeInputDTO());

        assertNotNull(response);
        assertEquals(10, response.getId());
    }


    @Test
    void createEmployee_noEmployees_throwsException() {

        doNothing().when(tokenService).decrypt(anyString());

        when(restTemplate.exchange(
                eq("http://localhost:8080/employees"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(EmployeeCreateResponse[].class)
        )).thenReturn(ResponseEntity.ok(new EmployeeCreateResponse[]{}));

        assertThrows(RuntimeException.class, () -> service.createEmployee("token", new EmployeeInputDTO()));
    }


    @Test
    void getDeviceBySerial_ok() {
        doNothing().when(tokenService).decrypt(anyString());
        DeviceDTO device = new DeviceDTO();
        device.setAssignedTo(1);
        when(restTemplate.exchange(contains("devices/serial-number/ABC123"), eq(HttpMethod.GET), any(HttpEntity.class), eq(DeviceDTO.class))).thenReturn(ResponseEntity.ok(device));
        DeviceDTO response = service.getDeviceBySerial("token", "ABC123");
        assertNotNull(response);
        assertEquals(1, response.getAssignedTo());
    }


    @Test
    void createDevice_ok() {

        doNothing().when(tokenService).decrypt(anyString());

        when(restTemplate.exchange(
                eq("http://localhost:8081/devices"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> service.createDevice("token", new DeviceAdd()));
    }


    @Test
    void assignDevice_ok() {

        doNothing().when(tokenService).decrypt(anyString());

        DeviceDTO device = new DeviceDTO();
        device.setAssignedTo(5);

        when(restTemplate.exchange(
                eq("http://localhost:8081/devices"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        )).thenReturn(ResponseEntity.ok(device));

        DeviceDTO response =
                service.assignDevice("token", new UpdateAssignedToRequest());

        assertNotNull(response);
        assertEquals(5, response.getAssignedTo());
    }

}