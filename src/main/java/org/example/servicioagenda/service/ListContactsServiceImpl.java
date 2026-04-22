package org.example.servicioagenda.service;

import org.example.servicioagenda.dto.request.*;
import org.example.servicioagenda.security.TokenFilter;
import org.example.servicioagenda.dto.response.*;
import org.example.servicioagenda.mapper.AgendaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.*;
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
    public void assignDevice(String token, UpdateAssignedToRequest req) {
        String url = "http://localhost:8081/devices";

        HttpEntity<?> entity = buildEntity(token, req);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    @Override
    public ResponseEntity<Void> updateEmployee(String token, EmployeeInputDTO emp) {

        String url = "http://localhost:8080/employees";

        Map<String, Object> body = new HashMap<>();
        body.put("employees", List.of(emp));

        HttpEntity<?> entity = buildEntity(token, body);

        return restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    @Override
    public DeviceDTO getDeviceByAssigned(String token, Integer employeeId) {
        String url = "http://localhost:8081/devices/assignation/" + employeeId;
        HttpEntity<?> entity = buildEntity(token, null);
        return restTemplate.exchange(url, HttpMethod.GET, entity, DeviceDTO.class).getBody();
    }

    @Override
    public void deleteEmployee(String token, Integer employeeId) {
        System.out.println("LLAMANDO DELETE A EMPLOYEE " + employeeId);
        String url = "http://localhost:8080/employees/id/" + employeeId;
        HttpEntity<?> entity = buildEntity(token, null);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    @Override
    public void unassignDeviceByDeviceId(String token, UpdateAssignedByDeviceIdRequest req) {
        String url = "http://localhost:8081/devices/unassign-by-id";
        HttpEntity<?> entity = buildEntity(token, req);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    @Override
    public UpdateEmployeesResult updateEmployees(
            String token,
            List<EmployeeInputDTO> employees) {

        UpdateEmployeesResult result = new UpdateEmployeesResult();

        for (EmployeeInputDTO emp : employees) {

            Integer employeeId = emp.getEmployeeId();

            try {

                if (Boolean.TRUE.equals(emp.getDeleteEmployee())) {

                    try {
                        DeviceDTO device = getDeviceByAssigned(token, employeeId);

                        AssignedDeviceInputDTO dto = new AssignedDeviceInputDTO();
                        dto.setSerialNumber(device.getSerialNumber());
                        dto.setAssignedTo(null);

                        UpdateAssignedToRequest req = new UpdateAssignedToRequest();
                        req.setDevices(List.of(dto));

                        assignDevice(token, req);

                    } catch (Exception e) {

                        System.out.println(
                                "No se pudo desasignar dispositivo de employee " + employeeId
                        );
                    }


                    System.out.println("BORRANDO EMPLOYEE " + employeeId);
                    deleteEmployee(token, employeeId);

                    result.setAnySuccess(true);
                    continue;
                }

                if (Boolean.TRUE.equals(emp.getDeleteAssignedDevice())) {
                    try {
                        DeviceDTO device = getDeviceByAssigned(token, employeeId);

                        AssignedDeviceInputDTO dto = new AssignedDeviceInputDTO();
                        dto.setSerialNumber(device.getSerialNumber());
                        dto.setAssignedTo(null);

                        UpdateAssignedToRequest req = new UpdateAssignedToRequest();
                        req.setDevices(List.of(dto));

                        System.out.println("SERIAL A DESASIGNAR = " + device.getSerialNumber());

                        assignDevice(token, req);

                        result.setAnySuccess(true);

                    } catch (HttpClientErrorException.NotFound e) {
                        System.out.println(
                                "Empleado " + employeeId + " no tiene dispositivo asignado"
                        );
                    }
                }

                ResponseEntity<?> response = updateEmployee(token, emp);
                if (response.getStatusCode().is2xxSuccessful()) {
                    result.setAnySuccess(true);
                }

                if (emp.getAddDevice() != null) {

                    String sn = emp.getAddDevice().getSerialNumber();

                    try {
                        DeviceDTO existing = getDeviceBySerial(token, sn);

                        if (existing.getAssignedTo() != null) {
                            result.getErrors().add(new ContactErrorDto(
                                    String.valueOf(employeeId),
                                    "No se puede asignar el dispositivo. " + sn + " ya está asignado"
                            ));
                        } else {

                            AssignedDeviceInputDTO dto = new AssignedDeviceInputDTO();
                            dto.setSerialNumber(sn);
                            dto.setAssignedTo(employeeId);

                            UpdateAssignedToRequest assignReq = new UpdateAssignedToRequest();
                            assignReq.setDevices(List.of(dto));

                            assignDevice(token, assignReq);

                        }

                    } catch (HttpClientErrorException.NotFound nf) {
                        try {
                            AssignedDeviceInputDTO input = emp.getAddDevice();

                            DeviceAdd dev = new DeviceAdd();
                            dev.setSerialNumber(input.getSerialNumber());
                            dev.setBrand(input.getBrand());
                            dev.setModel(input.getModel());
                            dev.setOperatingSystem(input.getOperatingSystem());
                            dev.setAssignedTo(employeeId);

                            createDevice(token, dev);

                        } catch (Exception e) {
                            result.getErrors().add(new ContactErrorDto(
                                    String.valueOf(employeeId),
                                    "Error al crear el dispositivo " + sn
                            ));
                        }
                    }
                }

            } catch (Exception e) {
                result.getErrors().add(new ContactErrorDto(
                        String.valueOf(employeeId),
                        e.getMessage()
                ));
            }
        }

        return result;
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
