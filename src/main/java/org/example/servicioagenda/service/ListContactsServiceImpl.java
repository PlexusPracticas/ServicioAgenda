package org.example.servicioagenda.service;

import org.example.servicioagenda.dto.request.DeviceAdd;
import org.example.servicioagenda.dto.request.EmployeeInputDTO;
import org.example.servicioagenda.dto.request.UpdateAssignedToRequest;
import org.example.servicioagenda.dto.response.*;
import org.example.servicioagenda.mapper.AgendaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
@Service
public class ListContactsServiceImpl implements ListContactsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AgendaMapper mapper;

    @Override
    public AgendaResponse listEmployees(int page, int size) {
        String urlEmployees ="http://localhost:8080/employees?page="+page+"&size="+size;
        //String urlEmployees = "http://localhost:8080/employees?page="+page+"&size="+size;
        EmployeeServiceResponse external =
                restTemplate.getForObject(urlEmployees, EmployeeServiceResponse.class);
        if(external.getEmployees()==null || external.getEmployees().isEmpty()){
            AgendaResponse empty=new AgendaResponse();
            empty.setEmployee(List.of());
            empty.setTotalPages(0);
            empty.setTotalElements(0);
            return empty;
        }
        List<EmployeeAgendaDTO> contactos = external.getEmployees()
                .stream()
                .map(mapper::toAgenda)
                .toList();
        for(EmployeeAgendaDTO empleado:contactos){
            String urlDevice="http://localhost:8081/devices/assignation/"+empleado.getEmployeeId();

            try{
                DeviceDTO device=restTemplate.getForObject(urlDevice,DeviceDTO.class);
                empleado.setAssignedDevice(device);
            }catch (HttpClientErrorException.NotFound e){
                empleado.setAssignedDevice(null);
            }catch (Exception e){
                throw e;
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

        // Por cada empleado obtenemos un dispositivo asignado
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
        return restTemplate.postForObject(url, req, EmployeeCreateResponse.class);
    }


    @Override
    public DeviceDTO getDeviceBySerial(String serialNumber) {
        String url = "http://localhost:8081/devices/serial-number/" + serialNumber;
        return restTemplate.getForObject(url, DeviceDTO.class);
    }

    @Override
    public DeviceDTO createDevice(DeviceAdd req) {
        return restTemplate.postForObject("http://localhost:8081/devices", req, DeviceDTO.class);
    }

    @Override
    public DeviceDTO assignDevice(UpdateAssignedToRequest req) {
        String url = "http://localhost:8081/devices";
        restTemplate.put(url, req);
        return null;
    }


}
