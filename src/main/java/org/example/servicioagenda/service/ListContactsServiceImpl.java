package org.example.servicioagenda.service;

import org.example.servicioagenda.dto.request.DeviceAdd;
import org.example.servicioagenda.dto.request.EmployeeInputDTO;
import org.example.servicioagenda.dto.request.UpdateAssignedToRequest;
import org.example.servicioagenda.dto.response.*;
import org.example.servicioagenda.mapper.AgendaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ListContactsServiceImpl implements ListContactsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AgendaMapper mapper;

    @Override
    public AgendaResponse listEmployees(int page, int size) {
        String urlEmployees = "http://localhost:8080/employees?page=" + page + "&size=" + size;
        //String urlEmployees = "http://localhost:8080/employees?page="+page+"&size="+size;
        System.out.println("URL EMPLOYEES → " + urlEmployees);
        EmployeeServiceResponse external = restTemplate.getForObject(urlEmployees, EmployeeServiceResponse.class);
        if (external.getEmployees() == null || external.getEmployees().isEmpty()) {
            AgendaResponse empty = new AgendaResponse();
            empty.setEmployee(List.of());
            empty.setTotalPages(0);
            empty.setTotalElements(0);
            return empty;
        }
        List<EmployeeAgendaDTO> contactos = external.getEmployees()
                .stream()
                .map(mapper::toAgenda)
                .toList();
        for (EmployeeAgendaDTO empleado : contactos) {
            String urlDevice = "http://localhost:8081/devices/assignation/" + empleado.getEmployeeId();

            try {
                DeviceDTO device = restTemplate.getForObject(urlDevice, DeviceDTO.class);
                empleado.setAssignedDevice(device);
            } catch (HttpClientErrorException.NotFound e) {
                empleado.setAssignedDevice(null);
            } catch (Exception e) {
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
    public AgendaResponse filterEmployees(String filterValue, String filterType, int page, int size) {
        String urlEmployees = "http://localhost:8080/employees/search?" + filterType + "=" + filterValue + "&page=" + page + "&size=" + size;

        EmployeeServiceResponse external = restTemplate.getForObject(urlEmployees, EmployeeServiceResponse.class);

        // Si no hay resultados nos da lista vacía
        if (external.getEmployees() == null || external.getEmployees().isEmpty()) {
            AgendaResponse empty = new AgendaResponse();
            empty.setEmployee(List.of());
            empty.setTotalPages(0);
            empty.setTotalElements(0);
            return empty;
        }

        // Mapeamos
        List<EmployeeAgendaDTO> contactos = external.getEmployees()
                .stream()
                .map(mapper::toAgenda)
                .toList();

        //obtenemos un dispositivo asignado por empleado
        for (EmployeeAgendaDTO empleado : contactos) {
            String urlDevice = "http://localhost:8081/devices/assignation/" + empleado.getEmployeeId();
            try {
                DeviceDTO device = restTemplate.getForObject(urlDevice, DeviceDTO.class);
                empleado.setAssignedDevice(device);

            } catch (HttpClientErrorException.NotFound e) {
                empleado.setAssignedDevice(null);  //continua

            } catch (Exception e) {
                throw e; //devuelve 500
            }
        }
        AgendaResponse response = new AgendaResponse();
        response.setEmployee(contactos);
        response.setTotalPages(external.getTotalPages());
        response.setTotalElements(external.getTotalElements());

        return response;
    }


    @Override
    public EmployeeCreateResponse createEmployee(EmployeeInputDTO req) {
        String url = "http://localhost:8080/employees";

        //crea el body que espera el microservicio employees
        Map<String, Object> body = new HashMap<>();
        body.put("employees", List.of(req));

        //devuelve una lista de empleados creados
        EmployeeCreateResponse[] response =
                restTemplate.postForObject(
                        url,
                        body,
                        EmployeeCreateResponse[].class
                );

        //devolvemos el primero
        return response[0];

    }


    @Override
    public DeviceDTO getDeviceBySerial(String serialNumber) {
        String url = "http://localhost:8081/devices/serial-number/" + serialNumber;
        return restTemplate.getForObject(url, DeviceDTO.class);
    }

    @Override
    public void createDevice(DeviceAdd devReq) {

        String url = "http://localhost:8081/devices";

        Map<String, Object> body = new HashMap<>();
        body.put("devices", List.of(devReq));

        // llama al microservicio devices
        restTemplate.postForObject(url, body, Object.class);

    }

    @Override
    public DeviceDTO assignDevice(UpdateAssignedToRequest req) {
        String url = "http://localhost:8081/devices";
        restTemplate.put(url, req);
        return null;
    }


    @Override
    public void updateEmployee(EmployeeInputDTO emp) {

        Integer employeeId = emp.getEmployeeId();
        String sn = null;

        if (employeeId == null) {
            throw new RuntimeException("employeeId es obligatorio");
        }

        //DELETE EMPLOYEe
        if (Boolean.TRUE.equals(emp.getDeleteEmployee())) {

            try {
                DeviceDTO device = restTemplate.getForObject("http://localhost:8081/devices/assignation/" + employeeId, DeviceDTO.class);

                if (device != null) {
                    Map<String, Object> dev = new HashMap<>();
                    dev.put("assignedTo", null);

                    Map<String, Object> body = new HashMap<>();
                    body.put("devices", List.of(dev));


                    restTemplate.put("http://localhost:8081/devices", body);
                }
            } catch (HttpClientErrorException.NotFound e) {
                System.out.println("error");
            }

            restTemplate.delete("http://localhost:8080/employees/id/" + employeeId);
            return;
        }

        //UPDATE EMPLOYEE
        Map<String, Object> body = new HashMap<>();
        body.put("employees", List.of(emp));
        restTemplate.put("http://localhost:8080/employees", body);

        //DELETE ASSIGNED DEVICE
        if (Boolean.TRUE.equals(emp.getDeleteAssignedDevice())) {
            try {
                DeviceDTO device = restTemplate.getForObject("http://localhost:8081/devices/assignation/" + employeeId, DeviceDTO.class);

                if (device != null && emp.getAddDevice() != null) {
                    sn = emp.getAddDevice().getSerialNumber();
                }

                if (sn != null) {
                    Map<String, Object> bodyDev = new HashMap<>();
                    bodyDev.put("serialNumber", sn);
                    bodyDev.put("assignedTo", null);

                    restTemplate.put("http://localhost:8081/devices", bodyDev);
                }

            } catch (HttpClientErrorException.NotFound e) {
                // seguimos
            }
        }

        //ADD DEVICE
        if (emp.getAddDevice() != null) {

            sn = emp.getAddDevice().getSerialNumber(); //

            try {
                DeviceDTO existing = restTemplate.getForObject("http://localhost:8081/devices/serial-number/" + sn, DeviceDTO.class);

                if (existing.getAssignedTo() != null) {
                    throw new RuntimeException("No se puede asignar el dispositivo " + sn + " ya está asignado");
                }


                Map<String, Object> device = new HashMap<>();
                device.put("serialNumber", sn);
                device.put("assignedTo", employeeId);

                Map<String, Object> bodydev = new HashMap<>();
                bodydev.put("devices", List.of(device));

                restTemplate.put("http://localhost:8081/devices", bodydev);


            } catch (HttpClientErrorException.NotFound e) {

                DeviceAdd dev = new DeviceAdd();
                dev.setSerialNumber(sn);
                dev.setBrand(emp.getAddDevice().getBrand());
                dev.setModel(emp.getAddDevice().getModel());
                dev.setOperatingSystem(emp.getAddDevice().getOperatingSystem());
                dev.setAssignedTo(employeeId);

                Map<String, Object> bodyDev = new HashMap<>();
                bodyDev.put("devices", List.of(dev));

                restTemplate.postForObject("http://localhost:8081/devices", bodyDev, Object.class);
            }
        }
    }


}
