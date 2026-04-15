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

@RestController
@RequestMapping("/agenda")
public class AgendaController {

    private ListContactsService agendaService;

    public AgendaController(ListContactsService agendaService){
        this.agendaService=agendaService;
    }

    @GetMapping("/employees")
    public ResponseEntity<?> listAgendaEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            AgendaResponse response = agendaService.listEmployees(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorDTO("Se ha producido un error técnico, pruebe de nuevo"));
        }
    }


    @GetMapping("/employees/filter/{filterValue}")
    public ResponseEntity<?> filterAgendaEmployees(
            @PathVariable String filterValue,
            @RequestParam String filterType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            AgendaResponse response = agendaService.filterEmployees(filterValue, filterType, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorDTO("Se ha producido un error técnico, pruebe de nuevo"));
        }
    }


    @PostMapping("/employees")
    public ResponseEntity<?> agregarContactos(@RequestBody CreateContactDto request) {
        List<ContactErrorDto> errores = new ArrayList<>();
        boolean algunExito = false;
        for (EmployeeAdd contacto : request.getEmployees()) {
            String employeeIdStr = null;
            try {
                // 1️⃣ Crear employee
                EmployeeInputDTO empReq = new EmployeeInputDTO();
                empReq.setName(contacto.getName());
                empReq.setSurname(contacto.getSurname());
                empReq.setMailPlexus(contacto.getPlexusMail());
                empReq.setMailClient(contacto.getMailClient());
                empReq.setPhoneNumber(contacto.getPhoneNumber());
                EmployeeCreateResponse empCreated =
                        agendaService.createEmployee(empReq);
                Integer employeeId = empCreated.getId();
                employeeIdStr = "Contacto agregado con id " + employeeId;
                algunExito = true;
                // 2️⃣ Si NO tiene dispositivo asignado → pasamos al siguiente
                if (contacto.getAssignedDevice() == null)
                    continue;
                String sn = contacto.getAssignedDevice().getSerialNumber();
                try {
                    // 3️⃣ GET device
                    DeviceDTO existing =
                            agendaService.getDeviceBySerial(sn);
                    // 3.1️⃣ si assignedTo != null → ya asignado → ERROR
                    if (existing.getAssignedTo() != null) {
                        errores.add(new ContactErrorDto(employeeIdStr, "No se puede asignar el dispositivo " + sn + ". Ya está asignado a otro contacto con id " + existing.getAssignedTo()));
                        continue;
                    }
                    // 3.2️⃣ assignedTo == null → asignar
                    UpdateAssignedToRequest assignReq = new UpdateAssignedToRequest();
                    assignReq.setSerialNumber(sn);
                    assignReq.setAssignedTo(employeeId);
                    try {
                        agendaService.assignDevice(assignReq);
                    } catch (Exception e) {
                        errores.add(new ContactErrorDto(
                                employeeIdStr,
                                "Error técnico - No se pudo asignar el dispositivo " + sn
                        ));
                    }
                } catch (HttpClientErrorException.NotFound nf) {
                    // 4️⃣ Si GET devuelve 404 → crear device
                    try {
                        DeviceAdd devReq = new DeviceAdd();
                        devReq.setSerialNumber(contacto.getAssignedDevice().getSerialNumber());
                        devReq.setBrand(contacto.getAssignedDevice().getBrand());
                        devReq.setModel(contacto.getAssignedDevice().getModel());
                        devReq.setOperatingSystem(contacto.getAssignedDevice().getOperatingSystem());
                        devReq.setAssignedTo(employeeId);
                        agendaService.createDevice(devReq);
                    } catch (Exception e) {
                        errores.add(new ContactErrorDto(
                                employeeIdStr,
                                "Error al crear dispositivo " + sn
                        ));
                    }
                } catch (Exception e) {
                    errores.add(new ContactErrorDto(
                            employeeIdStr,
                            "Error técnico - No se pudo obtener información del dispositivo " + contacto.getAssignedDevice().getSerialNumber()
                    ));
                }
            } catch (Exception e) {
                errores.add(new ContactErrorDto(
                        null,
                        "Error al agregar al contacto " +
                                contacto.getName() + " " + contacto.getSurname()
                ));
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


}