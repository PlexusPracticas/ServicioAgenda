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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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

    @InjectMocks
    private ListContactsServiceImpl service;

    @Test
    void listEmployees_ok() {

        EmployeeServiceResponse external = new EmployeeServiceResponse();
        external.setEmployees(List.of());
        external.setTotalElements(0);
        external.setTotalPages(0);

        when(restTemplate.getForObject(anyString(), eq(EmployeeServiceResponse.class)))
                .thenReturn(external);

        AgendaResponse response = service.listEmployees(0, 10);

        assertNotNull(response);
        assertTrue(response.getEmployee().isEmpty());
    }

    @Test
    void listEmployees_devicesNotFound() {

        EmployeeExternalDTO emp = new EmployeeExternalDTO();
        emp.setId(1);

        EmployeeServiceResponse external = new EmployeeServiceResponse();
        external.setEmployees(List.of(emp));
        external.setTotalElements(1);
        external.setTotalPages(1);

        EmployeeAgendaDTO agendaDTO = new EmployeeAgendaDTO();
        agendaDTO.setEmployeeId(1);

        when(restTemplate.getForObject(contains("employees"), eq(EmployeeServiceResponse.class)))
                .thenReturn(external);

        when(mapper.toAgenda(any()))
                .thenReturn(agendaDTO);

        when(restTemplate.getForObject(contains("devices/assignation"),
                eq(DeviceDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        AgendaResponse response = service.listEmployees(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getEmployee().size());
        assertNull(response.getEmployee().get(0).getAssignedDevice());
    }

    @Test
    void filterEmployees_ok() {

        EmployeeServiceResponse external = new EmployeeServiceResponse();
        external.setEmployees(List.of());
        external.setTotalElements(0);
        external.setTotalPages(0);

        when(restTemplate.getForObject(anyString(), eq(EmployeeServiceResponse.class)))
                .thenReturn(external);

        AgendaResponse response =
                service.filterEmployees("Luisa", "name", 0, 10);

        assertNotNull(response);
        assertTrue(response.getEmployee().isEmpty());
    }


    @Test
    void createEmployee_ok() {

        EmployeeCreateResponse created = new EmployeeCreateResponse();
        created.setId(1);

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(EmployeeCreateResponse[].class)))
                .thenReturn(new EmployeeCreateResponse[]{created});

        EmployeeCreateResponse response =
                service.createEmployee(new EmployeeInputDTO());

        assertNotNull(response);
        assertEquals(1, response.getId());
    }


    @Test
    void getDeviceBySerial_ok() {

        DeviceDTO device = new DeviceDTO();
        device.setAssignedTo(1);

        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class)))
                .thenReturn(device);

        DeviceDTO response = service.getDeviceBySerial("ABC123");

        assertNotNull(response);
    }


    @Test
    void createDevice_ok() {

        doReturn(null).when(restTemplate)
                .postForObject(anyString(), any(), eq(Object.class));

        service.createDevice(new DeviceAdd());

        verify(restTemplate, times(1))
                .postForObject(anyString(), any(), eq(Object.class));
    }


    @Test
    void assignDevice_ok() {

        doNothing().when(restTemplate)
                .put(anyString(), any(UpdateAssignedToRequest.class));

        DeviceDTO response =
                service.assignDevice(new UpdateAssignedToRequest());

        assertNull(response);
    }


    @Test
    void updateEmployee_deleteEmployee_ok() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);
        emp.setDeleteEmployee(true);
        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        assertThrows(HttpClientErrorException.class, () -> service.updateEmployee(emp));


    }

    @Test
    void updateEmployee_updateOnly_ok() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);

        doNothing().when(restTemplate)
                .put(contains("employees"), any());

        assertDoesNotThrow(() -> service.updateEmployee(emp));
    }

    @Test
    void updateEmployee_addDevice_notExists_ok() {
        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);
        AssignedDeviceInputDTO add = new AssignedDeviceInputDTO();
        add.setSerialNumber("ABC123");
        emp.setAddDevice(add);
        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        assertThrows(HttpClientErrorException.class, () -> service.updateEmployee(emp));

    }

    @Test
    void updateEmployee_deleteAssignedDevice_ok() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);
        emp.setDeleteAssignedDevice(true);
        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class))).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        doNothing().when(restTemplate).put(anyString(), any());
        assertDoesNotThrow(() -> service.updateEmployee(emp));
    }


    @Test
    void updateEmployee_addDevice_alreadyAssigned_throwsException() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);

        AssignedDeviceInputDTO add = new AssignedDeviceInputDTO();
        add.setSerialNumber("ABC123");
        emp.setAddDevice(add);

        DeviceDTO existing = new DeviceDTO();
        existing.setAssignedTo(99);

        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class)))
                .thenReturn(existing);

        assertThrows(RuntimeException.class,
                () -> service.updateEmployee(emp));
    }
    @Test
    void updateEmployee_employeeIdNull_throwsException() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(null);

        assertThrows(RuntimeException.class,
                () -> service.updateEmployee(emp));
    }

    @Test
    void updateEmployee_deleteEmployee_deviceNotFound_ok() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);
        emp.setDeleteEmployee(true);

        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class)))
                .thenThrow(
                        HttpClientErrorException.NotFound.create(
                                HttpStatus.NOT_FOUND, "Not Found", null, null, null
                        )
                );

        doNothing().when(restTemplate)
                .delete(anyString());

        assertDoesNotThrow(() -> service.updateEmployee(emp));
    }
    @Test
    void updateEmployee_deleteEmployee_deviceExists_ok() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);
        emp.setDeleteEmployee(true);

        DeviceDTO device = new DeviceDTO();
        device.setAssignedTo(1);

        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class)))
                .thenReturn(device);

        doNothing().when(restTemplate)
                .put(anyString(), any());

        doNothing().when(restTemplate)
                .delete(anyString());

        assertDoesNotThrow(() -> service.updateEmployee(emp));
    }

    @Test
    void updateEmployee_deleteAssignedDevice_deviceNotFound_ok() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);
        emp.setDeleteAssignedDevice(true);

        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class)))
                .thenThrow(
                        HttpClientErrorException.NotFound.create(
                                HttpStatus.NOT_FOUND, "Not Found", null, null, null
                        )
                );

        doNothing().when(restTemplate)
                .put(anyString(), any());

        assertDoesNotThrow(() -> service.updateEmployee(emp));
    }
    @Test
    void updateEmployee_addDevice_exists_notAssigned_ok() {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);

        AssignedDeviceInputDTO add = new AssignedDeviceInputDTO();
        add.setSerialNumber("ABC123");
        emp.setAddDevice(add);

        DeviceDTO existing = new DeviceDTO();
        existing.setAssignedTo(null);

        when(restTemplate.getForObject(anyString(), eq(DeviceDTO.class)))
                .thenReturn(existing);

        doNothing().when(restTemplate)
                .put(anyString(), any());

        doNothing().when(restTemplate)
                .put(contains("employees"), any());

        assertDoesNotThrow(() -> service.updateEmployee(emp));
    }


}