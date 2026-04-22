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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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
    void listarEmpleados_sinResultados() {

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
    void listarEmpleados_sinDispositivo() {

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
    void listarEmpleados_conDispositivo() {

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
    void filtrarEmpleados_sinResultados() {

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
    void filtrarEmpleados_conDispositivo() {
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
    void crearEmpleado_sinRespuesta_error() {

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
    void obtenerDispositivo_porSerial_ok() {
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

        when(restTemplate.exchange(
                eq("http://localhost:8081/devices"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        assertDoesNotThrow(() ->
                service.assignDevice("token", new UpdateAssignedToRequest())
        );
    }

    @Test
    void updateEmployee_ok() {

        // given
        doNothing().when(tokenService).decrypt(anyString());

        ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                eq("http://localhost:8080/employees"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(responseEntity);

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setName("Juan");
        emp.setSurname("Perez");

        // when
        ResponseEntity<Void> response = service.updateEmployee("token", emp);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(tokenService).decrypt("token");
        verify(restTemplate).exchange(
                eq("http://localhost:8080/employees"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }


    @Test
    void updateEmployee_errorServidor() {

        doNothing().when(tokenService).decrypt(anyString());

        when(restTemplate.exchange(
                eq("http://localhost:8080/employees"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setName("Error");

        assertThrows(HttpServerErrorException.class, () ->
                service.updateEmployee("token", emp)
        );
    }

    @Test
    void updateEmployee_tokenInvalido() {

        doThrow(new RuntimeException("Token inválido"))
                .when(tokenService).decrypt(anyString());

        EmployeeInputDTO emp = new EmployeeInputDTO();

        assertThrows(RuntimeException.class, () ->
                service.updateEmployee("token", emp)
        );

        verify(restTemplate, never()).exchange(
                anyString(), any(), any(), eq(Void.class)
        );
    }


    @Test
    void getDeviceByAssigned_ok() {

        // given
        doNothing().when(tokenService).decrypt(anyString());

        DeviceDTO device = new DeviceDTO();
        device.setAssignedTo(5);

        when(restTemplate.exchange(
                eq("http://localhost:8081/devices/assignation/5"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        )).thenReturn(ResponseEntity.ok(device));


        DeviceDTO result = service.getDeviceByAssigned("token", 5);


        assertNotNull(result);
        assertEquals(5, result.getAssignedTo());

        verify(tokenService).decrypt("token");
        verify(restTemplate).exchange(
                eq("http://localhost:8081/devices/assignation/5"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        );
    }


    @Test
    void getDeviceByAssigned_noExiste_404() {

        doNothing().when(tokenService).decrypt(anyString());

        when(restTemplate.exchange(
                eq("http://localhost:8081/devices/assignation/10"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        )).thenThrow(
                HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        null,
                        null
                )
        );

        assertThrows(HttpClientErrorException.NotFound.class, () ->
                service.getDeviceByAssigned("token", 10)
        );

        verify(tokenService).decrypt("token");
    }


    @Test
    void getDeviceByAssigned_tokenInvalido() {

        doThrow(new RuntimeException("Token inválido"))
                .when(tokenService).decrypt(anyString());

        assertThrows(RuntimeException.class, () ->
                service.getDeviceByAssigned("token", 1)
        );

        verify(restTemplate, never()).exchange(
                anyString(),
                any(),
                any(),
                eq(DeviceDTO.class)
        );
    }


    @Test
    void deleteEmployee_ok() {

        // given
        doNothing().when(tokenService).decrypt(anyString());

        when(restTemplate.exchange(
                eq("http://localhost:8080/employees/id/5"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.noContent().build());

        // when / then
        assertDoesNotThrow(() ->
                service.deleteEmployee("token", 5)
        );

        verify(tokenService).decrypt("token");
        verify(restTemplate).exchange(
                eq("http://localhost:8080/employees/id/5"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }


    @Test
    void deleteEmployee_noExiste_404() {

        doNothing().when(tokenService).decrypt(anyString());

        when(restTemplate.exchange(
                eq("http://localhost:8080/employees/id/10"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(
                HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        null,
                        null
                )
        );

        assertThrows(HttpClientErrorException.NotFound.class, () ->
                service.deleteEmployee("token", 10)
        );

        verify(tokenService).decrypt("token");
    }




    @Test
    void updateEmployees_deleteEmployee_conDispositivo() {

        doNothing().when(tokenService).decrypt(anyString());

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);
        emp.setDeleteEmployee(true);

        DeviceDTO device = new DeviceDTO();
        device.setSerialNumber("SN1");

        when(restTemplate.exchange(
                eq("http://localhost:8081/devices/assignation/1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        )).thenReturn(ResponseEntity.ok(device));

        when(restTemplate.exchange(
                eq("http://localhost:8081/devices"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        when(restTemplate.exchange(
                eq("http://localhost:8080/employees/id/1"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.noContent().build());

        // WHEN
        UpdateEmployeesResult result =
                service.updateEmployees("token", List.of(emp));

        assertTrue(result.isAnySuccess());
        assertTrue(result.getErrors().isEmpty());

        verify(restTemplate).exchange(
                eq("http://localhost:8081/devices/assignation/1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        );

        verify(restTemplate).exchange(
                eq("http://localhost:8081/devices"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );

        verify(restTemplate).exchange(
                eq("http://localhost:8080/employees/id/1"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }


    @Test
    void updateEmployees_deleteEmployee_sinDispositivo() {

        doNothing().when(tokenService).decrypt(anyString());

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(2);
        emp.setDeleteEmployee(true);


        when(restTemplate.exchange(
                eq("http://localhost:8081/devices/assignation/2"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DeviceDTO.class)
        )).thenThrow(HttpClientErrorException.NotFound.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));


        when(restTemplate.exchange(
                eq("http://localhost:8080/employees/id/2"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.noContent().build());

        UpdateEmployeesResult result =
                service.updateEmployees("token", List.of(emp));

        assertTrue(result.isAnySuccess());
        assertTrue(result.getErrors().isEmpty());

        verify(restTemplate).exchange(
                eq("http://localhost:8080/employees/id/2"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }



}