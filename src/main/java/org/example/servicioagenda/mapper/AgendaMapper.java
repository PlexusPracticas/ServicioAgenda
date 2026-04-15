package org.example.servicioagenda.mapper;

import org.example.servicioagenda.dto.response.EmployeeAgendaDTO;
import org.example.servicioagenda.dto.response.EmployeeExternalDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgendaMapper {

    default EmployeeAgendaDTO toAgenda(EmployeeExternalDTO entrada) {
        EmployeeAgendaDTO dto = new EmployeeAgendaDTO();
        dto.setEmployeeId(entrada.getId());
        dto.setName(entrada.getName());
        dto.setSurname(entrada.getSurname());
        dto.setMailPlexus(entrada.getMailPlexus());
        dto.setMailClient(entrada.getMailClient());
        dto.setPhoneNumber(entrada.getPhoneNumber());
        dto.setClientId(entrada.getClientId());
        return dto;
    }
}
