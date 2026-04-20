package org.example.servicioagenda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.servicioagenda.dto.request.*;
import org.example.servicioagenda.dto.response.AgendaResponse;
import org.example.servicioagenda.dto.response.DeviceDTO;
import org.example.servicioagenda.dto.response.EmployeeCreateResponse;
import org.example.servicioagenda.service.ListContactsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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


    @Test
    void listAgendaEmployees_ok() throws Exception {

        AgendaResponse response = new AgendaResponse();
        response.setEmployee(List.of());
        response.setTotalPages(0);
        response.setTotalElements(0);

        Mockito.when(agendaService.listEmployees(0, 10))
                .thenReturn(response);

        mockMvc.perform(get("/agenda/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee").exists());
    }


    @Test
    void listAgendaEmployees_error_returns500() throws Exception {

        Mockito.when(agendaService.listEmployees(0, 10))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/agenda/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void filterAgendaEmployees_ok() throws Exception {

        AgendaResponse response = new AgendaResponse();
        response.setEmployee(List.of());
        response.setTotalPages(0);
        response.setTotalElements(0);

        Mockito.when(agendaService.filterEmployees(
                        Mockito.eq("Luisa"),
                        Mockito.eq("name"),
                        Mockito.eq(0),
                        Mockito.eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/agenda/employees/filter/Luisa")
                        .param("filterType", "name")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee").exists());
    }


    @Test
    void addContacts_ok_returns201() throws Exception {

        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Gimli");
        emp.setSurname("Hijo de Gloin");
        emp.setMailPlexus("gimli@plexus.tech");

        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));


        EmployeeCreateResponse response = new EmployeeCreateResponse();
        response.setId(1);

        Mockito.when(
                agendaService.createEmployee(Mockito.any(EmployeeInputDTO.class))
        ).thenReturn(response);


        mockMvc.perform(post("/agenda/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }


    @Test
    void addContacts_allFail_returns500() throws Exception {

        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Gimli");
        emp.setSurname("Hijo de Gloin");
        emp.setMailPlexus("gimli@plexus.tech");

        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));

        Mockito.when(agendaService.createEmployee(Mockito.any()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/agenda/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.contactErrorDtos").exists());
    }

    @Test
    void updateContacts_ok_returns200() throws Exception {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(1);

        UpdateEmployeesRequest request = new UpdateEmployeesRequest();
        request.setEmployees(List.of(emp));

        Mockito.doNothing()
                .when(agendaService)
                .updateEmployee(Mockito.any());

        mockMvc.perform(put("/agenda/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Contactos actualizados correctamente"));
    }


    @Test
    void updateContacts_partial_returns206() throws Exception {

        EmployeeInputDTO ok = new EmployeeInputDTO();
        ok.setEmployeeId(1);

        EmployeeInputDTO fail = new EmployeeInputDTO();
        fail.setEmployeeId(2);

        UpdateEmployeesRequest request = new UpdateEmployeesRequest();
        request.setEmployees(List.of(ok, fail));

        Mockito.doNothing()
                .when(agendaService)
                .updateEmployee(Mockito.argThat(e -> e.getEmployeeId() == 1));

        Mockito.doThrow(new RuntimeException("Error"))
                .when(agendaService)
                .updateEmployee(Mockito.argThat(e -> e.getEmployeeId() == 2));

        mockMvc.perform(put("/agenda/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isPartialContent())
                .andExpect(jsonPath("$.contactErrorDtos").exists());
    }


    @Test
    void updateContacts_allFail_returns500() throws Exception {

        EmployeeInputDTO emp = new EmployeeInputDTO();
        emp.setEmployeeId(99);

        UpdateEmployeesRequest request = new UpdateEmployeesRequest();
        request.setEmployees(List.of(emp));

        Mockito.doThrow(new RuntimeException("Error"))
                .when(agendaService)
                .updateEmployee(Mockito.any());

        mockMvc.perform(put("/agenda/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.contactErrorDtos").exists());
    }

    @Test
    void addContacts_deviceAlreadyAssigned_returns204() throws Exception {

        AssignedDeviceInputDTO device = new AssignedDeviceInputDTO();
        device.setSerialNumber("ABC123");

        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Gimli");
        emp.setSurname("Hijo de Gloin");
        emp.setMailPlexus("gimli@plexus.tech");
        emp.setAssignedDevice(device);

        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));

        EmployeeCreateResponse response = new EmployeeCreateResponse();
        response.setId(1);

        DeviceDTO existing = new DeviceDTO();
        existing.setAssignedTo(99);

        Mockito.when(agendaService.createEmployee(Mockito.any()))
                .thenReturn(response);

        Mockito.when(agendaService.getDeviceBySerial("ABC123"))
                .thenReturn(existing);

        mockMvc.perform(post("/agenda/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.contactErrorDtos").exists());
    }

    @Test
    void addContacts_withoutDevice_returns201() throws Exception {
        EmployeeAdd emp = new EmployeeAdd();
        emp.setName("Gimli");
        emp.setSurname("Hijo de Gloin");
        emp.setMailPlexus("gimli@plexus.tech");
        CreateContactDto request = new CreateContactDto();
        request.setEmployees(List.of(emp));
        EmployeeCreateResponse response = new EmployeeCreateResponse();
        response.setId(1);
        Mockito.when(agendaService.createEmployee(Mockito.any())).thenReturn(response);
        mockMvc.perform(post("/agenda/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }


    @Test
    void filterAgendaEmployees_error_returns500() throws Exception {

        Mockito.when(agendaService.filterEmployees(
                Mockito.anyString(), Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyInt()
        )).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/agenda/employees/filter/test")
                        .param("filterType", "name")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("Se ha producido un error técnico, pruebe de nuevo"));
    }



}