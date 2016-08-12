package com.j32bit.ticket.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.j32bit.ticket.bean.Company;

@Path("company")
public class CompanyRest {

	private static Logger logger = LogManager.getLogger();
	@POST
	@Path("addNewCompany")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Error addNewCompany(Company company){
		
		logger.info("Company added : "+company);		
		return null;
	}
	
	
}