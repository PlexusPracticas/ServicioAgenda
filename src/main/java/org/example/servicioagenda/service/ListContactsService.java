package org.example.servicioagenda.service;

import org.example.servicioagenda.dto.request.DeviceAdd;
import org.example.servicioagenda.dto.request.EmployeeInputDTO;
import org.example.servicioagenda.dto.request.UpdateAssignedByDeviceIdRequest;
import org.example.servicioagenda.dto.request.UpdateAssignedToRequest;
import org.example.servicioagenda.dto.response.*;

import java.util.List;

public interface ListContactsService {

    // ya existentes
    AgendaResponse listEmployees(String token, int page, int size);
    AgendaResponse filterEmployees(String token, String filterValue, String filterType, int page, int size);

    EmployeeCreateResponse createEmployee(String token, EmployeeInputDTO req);
    void updateEmployee(String token, EmployeeInputDTO req);
    void deleteEmployee(String token, Integer employeeId);

    DeviceDTO getDeviceBySerial(String token, String serialNumber);
    DeviceDTO getDeviceByAssigned(String token, Integer employeeId);

    void createDevice(String token, DeviceAdd req);
    DeviceDTO assignDevice(String token, UpdateAssignedToRequest req);

    void unassignDeviceByDeviceId(String token, UpdateAssignedByDeviceIdRequest req);

    UpdateEmployeesResult updateEmployees(String token, List<EmployeeInputDTO> employees);

}