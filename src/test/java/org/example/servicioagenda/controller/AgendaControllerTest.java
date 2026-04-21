package org.example.servicioagenda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.servicioagenda.dto.request.*;
import org.example.servicioagenda.dto.response.AgendaResponse;
import org.example.servicioagenda.dto.response.DeviceDTO;
import org.example.servicioagenda.dto.response.EmployeeCreateResponse;
import org.example.servicioagenda.service.ListContactsService;
import org.example.servicioagenda.service.TokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AgendaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ListContactsService agendaService;

    @MockBean
    private TokenService tokenService;

    private static final String TOKEN = "fake-token";


    @Test
    void listarEmpleados_ok() throws Exception {
        AgendaResponse mockResponse = new AgendaResponse();
        Mockito.doNothing().when(tokenService).decrypt(TOKEN);
        Mockito.when(agendaService.listEmployees(TOKEN, 0, 10)).thenReturn(mockResponse);
        mockMvc.perform(get("/agenda/employees").header("TOKEN", TOKEN).param("page", "0").param("size", "10")).andExpect(status().isOk());
        Mockito.verify(agendaService).listEmployees(TOKEN, 0, 10);
    }

    @Test
    void listarAgendaEmpleados_paginacionDefecto() throws Exception {

        AgendaResponse mockResponse = new AgendaResponse();

        Mockito.doNothing().when(tokenService).decrypt(TOKEN);
        Mockito.when(agendaService.listEmployees(TOKEN, 0, 10))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/agenda/employees")
                        .header("TOKEN", TOKEN))
                .andExpect(status().isOk());

        Mockito.verify(agendaService)
                .listEmployees(TOKEN, 0, 10);
    }


    @Test
    void listarEmpleados_errorInterno() throws Exception {

        Mockito.doNothing().when(tokenService).decrypt(TOKEN);

        Mockito.when(agendaService.listEmployees(TOKEN, 0, 10))
                .thenThrow(new RuntimeException("Error BD"));

        mockMvc.perform(get("/agenda/employees")
                        .header("TOKEN", TOKEN))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("Se ha producido un error técnico, pruebe de nuevo"));
    }


    @Test
    void filtrarEmpleados_ok() throws Exception {

        AgendaResponse mockResponse = new AgendaResponse();

        Mockito.doNothing()
                .when(tokenService)
                .decrypt(TOKEN);

        Mockito.when(agendaService.filterEmployees(
                        TOKEN, "juan", "name", 0, 10))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/agenda/employees/filter/juan")
                        .header("TOKEN", TOKEN)
                        .param("filterType", "name")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        Mockito.verify(agendaService)
                .filterEmployees(TOKEN, "juan", "name", 0, 10);
    }


    @Test
    void filtrarEmpleados_paginacionDefecto() throws Exception {

        AgendaResponse mockResponse = new AgendaResponse();

        Mockito.doNothing()
                .when(tokenService)
                .decrypt(TOKEN);

        Mockito.when(agendaService.filterEmployees(
                        TOKEN, "juan", "surname", 0, 10))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/agenda/employees/filter/juan")
                        .header("TOKEN", TOKEN)
                        .param("filterType", "surname"))
                .andExpect(status().isOk());

        Mockito.verify(agendaService)
                .filterEmployees(TOKEN, "juan", "surname", 0, 10);
    }


    @Test
    void agregarContacto_ok() throws Exception {

        Mockito.doNothing().when(tokenService).decrypt(TOKEN);

        EmployeeCreateResponse empResponse = new EmployeeCreateResponse();
        empResponse.setId(1);

        Mockito.when(agendaService.createEmployee(
                        Mockito.eq(TOKEN),
                        Mockito.any(EmployeeInputDTO.class)))
                .thenReturn(empResponse);

        DeviceDTO existing = new DeviceDTO();
        existing.setAssignedTo(null);

        Mockito.when(agendaService.getDeviceBySerial(
                        Mockito.eq(TOKEN),
                        Mockito.eq("SN123")))
                .thenReturn(existing);

        Mockito.when(agendaService.assignDevice(
                        Mockito.eq(TOKEN),
                        Mockito.any(UpdateAssignedToRequest.class)))
                .thenReturn(null);

        AssignedDeviceInputDTO device = new AssignedDeviceInputDTO();
        device.setSerialNumber("SN123");

        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Juan");
        emp.setSurname("Perez");
        emp.setAssignedDevice(device);

        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));

        mockMvc.perform(post("/agenda/employees")
                        .header("TOKEN", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void agregarContacto_dispositivoOcupado_204() throws Exception {
        Mockito.doNothing().when(tokenService).decrypt(TOKEN);
        EmployeeCreateResponse empResponse = new EmployeeCreateResponse();
        empResponse.setId(1);
        Mockito.when(agendaService.createEmployee(Mockito.eq(TOKEN), Mockito.any(EmployeeInputDTO.class))).thenReturn(empResponse);
        DeviceDTO existing = new DeviceDTO();
        existing.setAssignedTo(99);
        Mockito.when(agendaService.getDeviceBySerial(TOKEN, "SN999")).thenReturn(existing);
        AssignedDeviceInputDTO device = new AssignedDeviceInputDTO();
        device.setSerialNumber("SN999");
        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Ana");
        emp.setSurname("Gomez");
        emp.setAssignedDevice(device);
        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));
        mockMvc.perform(post("/agenda/employees").header("TOKEN", TOKEN).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent()).andExpect(jsonPath("$.contactErrorDtos").exists());
    }


    @Test
    void agregarContacto_errorTotal_500() throws Exception {

        Mockito.doNothing().when(tokenService).decrypt(TOKEN);

        Mockito.when(agendaService.createEmployee(
                        Mockito.eq(TOKEN),
                        Mockito.any(EmployeeInputDTO.class)))
                .thenThrow(new RuntimeException("Error"));

        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Error");

        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));

        mockMvc.perform(post("/agenda/employees")
                        .header("TOKEN", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.contactErrorDtos").exists());
    }

    @Test
    void agregarContacto_sinDispositivo_201() throws Exception {
        Mockito.doNothing().when(tokenService).decrypt(TOKEN);
        EmployeeCreateResponse empResponse = new EmployeeCreateResponse();
        empResponse.setId(5);
        Mockito.when(agendaService.createEmployee(Mockito.eq(TOKEN), Mockito.any(EmployeeInputDTO.class))).thenReturn(empResponse);
        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Mario");
        emp.setSurname("Rossi");
        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));
        mockMvc.perform(post("/agenda/employees").header("TOKEN", TOKEN).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated());
    }


    @Test
    void agregarContacto_creaDispositivo201() throws Exception {

        Mockito.doNothing().when(tokenService).decrypt(TOKEN);

        EmployeeCreateResponse empResponse = new EmployeeCreateResponse();
        empResponse.setId(7);

        Mockito.when(agendaService.createEmployee(
                        Mockito.eq(TOKEN),
                        Mockito.any(EmployeeInputDTO.class)))
                .thenReturn(empResponse);

        Mockito.when(agendaService.getDeviceBySerial(
                        Mockito.eq(TOKEN),
                        Mockito.eq("SN404")))
                .thenThrow(HttpClientErrorException.NotFound
                        .create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        Mockito.doNothing()
                .when(agendaService)
                .createDevice(Mockito.eq(TOKEN), Mockito.any(DeviceAdd.class));

        AssignedDeviceInputDTO device = new AssignedDeviceInputDTO();
        device.setSerialNumber("SN404");

        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Luis");
        emp.setSurname("Garcia");
        emp.setAssignedDevice(device);

        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));

        mockMvc.perform(post("/agenda/employees")
                        .header("TOKEN", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void agregarContacto_errorAlCrearDispositivo_204() throws Exception {
        Mockito.doNothing().when(tokenService).decrypt(TOKEN);
        EmployeeCreateResponse empResponse = new EmployeeCreateResponse();
        empResponse.setId(8);
        Mockito.when(agendaService.createEmployee(Mockito.eq(TOKEN), Mockito.any(EmployeeInputDTO.class))).thenReturn(empResponse);
        Mockito.when(agendaService.getDeviceBySerial(Mockito.eq(TOKEN), Mockito.eq("SNFAIL"))).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));
        Mockito.doThrow(new RuntimeException("Error creando device")).when(agendaService).createDevice(Mockito.eq(TOKEN), Mockito.any(DeviceAdd.class));
        AssignedDeviceInputDTO device = new AssignedDeviceInputDTO();
        device.setSerialNumber("SNFAIL");
        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Error");
        emp.setAssignedDevice(device);
        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));
        mockMvc.perform(post("/agenda/employees").header("TOKEN", TOKEN).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent()).andExpect(jsonPath("$.contactErrorDtos").exists());
    }

}