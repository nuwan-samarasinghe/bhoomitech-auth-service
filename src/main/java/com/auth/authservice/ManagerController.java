package com.auth.authservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeController {
    @GetMapping(value = "/emp/all")
    public ResponseEntity<Object> getEmpAll(){}

    @GetMapping(value = "/emp/{emp-id}")
    public ResponseEntity<Object> getEmp(@PathVariable("emp-id") String parameter){}

    @PostMapping(value = "/emp")
    public ResponseEntity<Object> addEmp(@RequestBody Object emp){}

    @PutMapping(value = "/emp")
    public ResponseEntity<Object> addEmp(@RequestBody Object emp){}

    @DeleteMapping(value = "/emp/{emp-id}")
    public ResponseEntity<Object> addEmp(@PathVariable("emp-id") String parameter){}

    @PostMapping(value = "/leave")
    public ResponseEntity<Object> addLeave(@RequestBody Object emp){}

    @GetMapping(value = "/leave/{emp-id}")
    public ResponseEntity<Object> getLeaves(){}

}
