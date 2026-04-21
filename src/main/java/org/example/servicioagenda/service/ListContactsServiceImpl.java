package org.example.servicioagenda.service;

import org.example.servicioagenda.dto.request.UpdateEmployeesRequest;
import org.example.servicioagenda.security.TokenFilter;
import org.example.servicioagenda.dto.request.DeviceAdd;
import org.example.servicioagenda.dto.request.EmployeeInputDTO;
import org.example.servicioagenda.dto.request.UpdateAssignedToRequest;
import org.example.servicioagenda.dto.response.*;
import org.example.servicioagenda.mapper.AgendaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ListContactsServiceImpl implements ListContactsService {

    private RestTemplate restTemplate;

    private AgendaMapper mapper;

    private final TokenService tokenService;

    public ListContactsServiceImpl(RestTemplate restTemplate, AgendaMapper mapper, TokenService tokenService) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.tokenService = tokenService;
    }

    @Override
    public AgendaResponse listEmployees(String token, int page, int size) {
        String urlEmployees = "http://localhost:8080/employees?page=" + page + "&size=" + size;

        //String urlEmployees = "http://localhost:8080/employees?page="+page+"&size="+size;
        //EmployeeServiceResponse external = restTemplate.getForObject(urlEmployees, EmployeeServiceResponse.class);

        HttpEntity<?> entity = buildEntity(token, null);
        EmployeeServiceResponse external = restTemplate.exchange(urlEmployees, HttpMethod.GET, entity, EmployeeServiceResponse.class).getBody();
        if (external == null || external.getEmployees().isEmpty()) {
            return emptyAgenda();
        }
        List<EmployeeAgendaDTO> contactos = external.getEmployees().stream().map(mapper::toAgenda).toList();
        for (EmployeeAgendaDTO empleado : contactos) {
            String urlDevice = "http://localhost:8081/devices/assignation/" + empleado.getEmployeeId();
            try {
                DeviceDTO device = restTemplate.exchange(urlDevice, HttpMethod.GET, entity, DeviceDTO.class).getBody();
                empleado.setAssignedDevice(device);
            } catch (HttpClientErrorException.NotFound e) {
                empleado.setAssignedDevice(null);
            }
        }
        AgendaResponse response = new AgendaResponse();
        response.setEmployee(contactos);
        response.setTotalElements(external.getTotalElements());
        response.setTotalPages(external.getTotalPages());
        return response;

    }


    @Override
    public AgendaResponse filterEmployees(String token, String filterValue, String filterType, int page, int size) {
        String urlEmployees = "http://localhost:8080/employees/search?" + filterType + "=" + filterValue + "&page=" + page + "&size=" + size;
        HttpEntity<?> entity = buildEntity(token, null);
        EmployeeServiceResponse external = restTemplate.exchange(urlEmployees, HttpMethod.GET, entity, EmployeeServiceResponse.class).getBody();
        if (external == null || external.getEmployees().isEmpty()) {
            return emptyAgenda();
        }
        List<EmployeeAgendaDTO> contactos = external.getEmployees().stream().map(mapper::toAgenda).toList();
        for (EmployeeAgendaDTO empleado : contactos) {
            String urlDevice = "http://localhost:8081/devices/assignation/" + empleado.getEmployeeId();
            try {
                DeviceDTO device = restTemplate.exchange(urlDevice, HttpMethod.GET, entity, DeviceDTO.class).getBody();
                empleado.setAssignedDevice(device);
            } catch (HttpClientErrorException.NotFound e) {
                empleado.setAssignedDevice(null);
            }
        }
        AgendaResponse response = new AgendaResponse();
        response.setEmployee(contactos);
        response.setTotalElements(external.getTotalElements());
        response.setTotalPages(external.getTotalPages());
        return response;

    }


    @Override
    public EmployeeCreateResponse createEmployee(String token, EmployeeInputDTO req) {
        String url = "http://localhost:8080/employees";
        Map<String, Object> body = new HashMap<>();
        body.put("employees", List.of(req));
        HttpEntity<?> entity = buildEntity(token, body);
        EmployeeCreateResponse[] response = restTemplate.exchange(url, HttpMethod.POST, entity, EmployeeCreateResponse[].class).getBody();
        if (response == null || response.length == 0) {
            throw new RuntimeException("No se creó ningún empleado");
        }
        return response[0];

    }


    @Override
    public DeviceDTO getDeviceBySerial(String token, String serialNumber) {
        String url = "http://localhost:8081/devices/serial-number/" + serialNumber;

        HttpEntity<?> entity = buildEntity(token, null);
        return restTemplate.exchange(url, HttpMethod.GET, entity, DeviceDTO.class).getBody();

    }

    @Override
    public void createDevice(String token, DeviceAdd devReq) {

        String url = "http://localhost:8081/devices";

        Map<String, Object> body = new HashMap<>();
        body.put("devices", List.of(devReq));
        HttpEntity<?> entity = buildEntity(token, body);
        restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);

    }

    @Override
    public DeviceDTO assignDevice(String token, UpdateAssignedToRequest req) {
        String url = "http://localhost:8081/devices";

        HttpEntity<?> entity = buildEntity(token, req);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, DeviceDTO.class).getBody();

    }


    @Override
    public void updateEmployee(String token, EmployeeInputDTO emp) {


        if (emp.getEmployeeId() == null) {
            throw new RuntimeException("employeeId es obligatorio");
        }

        if (Boolean.TRUE.equals(emp.getDeleteEmployee())) {

            HttpEntity<?> entity = buildEntity(token, null);

            restTemplate.exchange(
                    "http://localhost:8080/employees/id/" + emp.getEmployeeId(),
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
            return;
        }


        HttpEntity<?> entity = buildEntity(token, emp);

        restTemplate.exchange(
                "http://localhost:8080/employees/id/"+emp.getEmployeeId(),
                HttpMethod.PUT,
                entity,
                Void.class
        );
    }

    private HttpEntity<?> buildEntity(String token, Object body) {
        tokenService.decrypt(token); // validación adicional

        HttpHeaders headers = new HttpHeaders();
        headers.set("TOKEN", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    }

    private AgendaResponse emptyAgenda() {
        AgendaResponse empty = new AgendaResponse();
        empty.setEmployee(List.of());
        empty.setTotalElements(0);
        empty.setTotalPages(0);
        return empty;
    }
}
