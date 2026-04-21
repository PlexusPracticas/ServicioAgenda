package org.example.servicioagenda.controller;

import org.example.servicioagenda.dto.request.*;
import org.example.servicioagenda.dto.response.*;
import org.example.servicioagenda.service.ListContactsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agenda")
public class AgendaController {

    private ListContactsService agendaService;

    public AgendaController(ListContactsService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping("/employees")
    public ResponseEntity<?> listAgendaEmployees(
            @RequestHeader("TOKEN") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            AgendaResponse response = agendaService.listEmployees(token,page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorDTO("Se ha producido un error técnico, pruebe de nuevo"));
        }
    }


    @GetMapping("/employees/filter/{filterValue}")
    public ResponseEntity<?> filterAgendaEmployees(
            @RequestHeader("TOKEN") String token,
            @PathVariable String filterValue,
            @RequestParam String filterType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            AgendaResponse response = agendaService.filterEmployees(token,filterValue, filterType, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorDTO("Se ha producido un error técnico, pruebe de nuevo"));
        }
    }


    @PostMapping("/employees")
    public ResponseEntity<?> agregarContactos(@RequestHeader("TOKEN") String token,@RequestBody CreateContactDto request) {
        List<ContactErrorDto> errores = new ArrayList<>();
        boolean algunExito = false;
        for (EmployeeAdd contacto : request.getEmployees()) {
            String employeeIdStr = null;
            try {
                //Crear employee
                EmployeeInputDTO empReq = new EmployeeInputDTO();
                empReq.setName(contacto.getName());
                empReq.setSurname(contacto.getSurname());
                empReq.setMailPlexus(contacto.getMailPlexus());
                empReq.setMailClient(contacto.getMailClient());
                empReq.setPhoneNumber(contacto.getPhoneNumber());

                EmployeeCreateResponse empCreated = agendaService.createEmployee(token,empReq);

                Integer employeeId = empCreated.getId();
                employeeIdStr = "Contacto agregado con id " + employeeId;
                algunExito = true;
                //pasamos al siguiente si no tiene dispositivo asignado
                if (contacto.getAssignedDevice() == null)
                    continue;
                String sn = contacto.getAssignedDevice().getSerialNumber();
                try {
                    //usamos el get device
                    DeviceDTO existing = agendaService.getDeviceBySerial(token,sn);

                    if (existing.getAssignedTo() != null) {
                        errores.add(new ContactErrorDto(employeeIdStr, "No se puede asignar el dispositivo " + sn + ". Ya está asignado a otro contacto con id " + existing.getAssignedTo()));
                        continue;
                    }
                    //assignedTo == null se asigna
                    UpdateAssignedToRequest assignReq = new UpdateAssignedToRequest();
                    assignReq.setSerialNumber(sn);
                    assignReq.setAssignedTo(employeeId);
                    try {
                        agendaService.assignDevice(token,assignReq);
                    } catch (Exception e) {
                        errores.add(new ContactErrorDto(employeeIdStr, "Error técnico - No se pudo asignar el dispositivo " + sn));
                    }
                } catch (HttpClientErrorException.NotFound nf) {
                    //Si GET devuelve 404 se crea device
                    try {
                        DeviceAdd devReq = new DeviceAdd();
                        devReq.setSerialNumber(contacto.getAssignedDevice().getSerialNumber());
                        devReq.setBrand(contacto.getAssignedDevice().getBrand());
                        devReq.setModel(contacto.getAssignedDevice().getModel());
                        devReq.setOperatingSystem(contacto.getAssignedDevice().getOperatingSystem());
                        devReq.setAssignedTo(employeeId);
                        agendaService.createDevice(token,devReq);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //errores.add(new ContactErrorDto(employeeIdStr, "Error al crear dispositivo " + sn));
                        errores.add(new ContactErrorDto(employeeIdStr, e.getMessage()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //errores.add(new ContactErrorDto(employeeIdStr, "Error técnico - No se pudo obtener información del dispositivo " + contacto.getAssignedDevice().getSerialNumber()));
                    errores.add(new ContactErrorDto(employeeIdStr, e.getMessage()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                //errores.add(new ContactErrorDto(null, "Error al agregar al contacto " + contacto.getName() + " " + contacto.getSurname()));
                errores.add(new ContactErrorDto(null, e.getMessage()));
            }
        }

        if (!algunExito) {
            return ResponseEntity.status(500).body(new ErrorListResponse(errores));
        }
        if (!errores.isEmpty()) {
            return ResponseEntity.status(204).body(new ErrorListResponse(errores));
        }
        return ResponseEntity.status(201).build();
    }


    @PutMapping("/employees")
    public ResponseEntity<?> updateContacts(
            @RequestHeader("TOKEN") String token,
            @RequestBody UpdateEmployeesRequest request) {

        List<ContactErrorDto> errors = new ArrayList<>();
        boolean anySuccess = false;

        for (EmployeeInputDTO emp : request.getEmployees()) {
            try {
                agendaService.updateEmployee(token,emp);
                anySuccess = true;
            } catch (Exception e) {
                errors.add(new ContactErrorDto(
                        String.valueOf(emp.getEmployeeId()),
                        e.getMessage()
                ));
            }
        }

        if (!anySuccess) {
            return ResponseEntity.status(500).body(new ErrorListResponse(errors));
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.status(206).body(new ErrorListResponse(errors));
        }
        return ResponseEntity.ok(Map.of("message", "Contactos actualizados correctamente")
        );
    }


}