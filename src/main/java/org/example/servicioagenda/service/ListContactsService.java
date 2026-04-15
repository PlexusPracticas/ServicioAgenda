package org.example.servicioagenda.service;

import org.example.servicioagenda.dto.request.DeviceAdd;
import org.example.servicioagenda.dto.request.EmployeeInputDTO;
import org.example.servicioagenda.dto.request.UpdateAssignedToRequest;
import org.example.servicioagenda.dto.response.AgendaResponse;
import org.example.servicioagenda.dto.response.DeviceDTO;
import org.example.servicioagenda.dto.response.EmployeeCreateResponse;
import org.example.servicioagenda.dto.response.EmployeeServiceResponse;

public interface ListContactsService {
    AgendaResponse listEmployees(int page,int size);
    AgendaResponse filterEmployees(String filterValue, String filterType, int page, int size);

    EmployeeCreateResponse createEmployee(EmployeeInputDTO req);
    DeviceDTO getDeviceBySerial(String serialNumber);
    DeviceDTO createDevice(DeviceAdd req);
    DeviceDTO assignDevice(UpdateAssignedToRequest req);

}
