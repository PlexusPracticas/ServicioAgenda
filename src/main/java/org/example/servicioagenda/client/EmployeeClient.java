package org.example.servicioagenda.client;

import org.example.servicioagenda.dto.request.CreateContactDto;
import org.example.servicioagenda.dto.response.EmployeeExternalDTO;
import org.example.servicioagenda.dto.response.EmployeeServiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="employeeclient",url="http://localhost:8080")
public interface EmployeeClient {

    @GetMapping("/employees")
    EmployeeServiceResponse getEmployee(@RequestParam(name = "page", defaultValue = "0") int page,
                                        @RequestParam(name = "size", defaultValue = "10") int size);

    @GetMapping("/employees/search")
    EmployeeServiceResponse getSearch(@RequestParam (value = "id",required = false)Integer id,
                                      @RequestParam (value = "name",required = false)String name,
                                      @RequestParam (value = "surname",required = false)String surname,
                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "10") int size);

    @PostMapping("/employees")
    EmployeeExternalDTO agendarEmployee(@RequestBody CreateContactDto employee);

}
