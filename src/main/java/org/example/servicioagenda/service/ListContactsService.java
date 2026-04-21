package org.example.servicioagenda.service;

import org.example.servicioagenda.dto.request.DeviceAdd;
import org.example.servicioagenda.dto.request.EmployeeInputDTO;
import org.example.servicioagenda.dto.request.UpdateAssignedToRequest;
import org.example.servicioagenda.dto.response.AgendaResponse;
import org.example.servicioagenda.dto.response.DeviceDTO;
import org.example.servicioagenda.dto.response.EmployeeCreateResponse;
import org.example.servicioagenda.dto.response.EmployeeServiceResponse;

public interface ListContactsService {
    AgendaResponse listEmployees(String token,int page,int size);
    AgendaResponse filterEmployees(String token,String filterValue, String filterType, int page, int size);

    EmployeeCreateResponse createEmployee(String token,EmployeeInputDTO req);
    DeviceDTO getDeviceBySerial(String token,String serialNumber);
    void createDevice(String token,DeviceAdd req);
    DeviceDTO assignDevice(String token,UpdateAssignedToRequest req);
    void updateEmployee(String token, EmployeeInputDTO req);
}