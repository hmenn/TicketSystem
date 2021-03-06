package com.j32bit.ticket.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.j32bit.ticket.bean.Company;

public class CompanyDAOService extends ConnectionHelper {

	private Logger logger = LogManager.getLogger(CompanyDAOService.class);

	public CompanyDAOService() {
		logger.info("CompanyDAOService constructed");
	}

	public void init(Properties prop) {
		logger.info("CompanyDAOService initialize started");
		super.init(prop);
		logger.info("CompanyDAOService initialize finished");
	}

	public Company addCompany(Company company) throws Exception {
		logger.debug("addCompany started");

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pst = null;
		StringBuilder query = new StringBuilder();
		StringBuilder queryLog = new StringBuilder();
		long recordId = 0;

		checkSimilarCompanyRecord(company);

		try {

			query.append("INSERT INTO companies ");
			query.append("(COMPANY_NAME,ADDRESS,EMAIL,PHONE,FAX) ");
			query.append("VALUES (?,?,?,?,?)");
			String queryString = query.toString();
			logger.debug("sql query created :" + queryString);

			con = getConnection();
			// auto incremenet index leri almak icin 2.parametre lazim
			pst = con.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);

			if (logger.isTraceEnabled()) { // trace bas sonra calistir
				queryLog.append("Query : ").append(queryString).append("\n");
				queryLog.append("Parameters : ").append("\n");
				queryLog.append("Name : ").append(company.getName()).append("\n");
				queryLog.append("Address : ").append(company.getAddress()).append("\n");
				queryLog.append("Email : ").append(company.getEmail()).append("\n");
				queryLog.append("Phone : ").append(company.getPhone()).append("\n");
				queryLog.append("Fax : ").append(company.getFax()).append("\n");
				logger.trace(queryLog.toString());
			}

			pst.setString(1, company.getName());
			pst.setString(2, company.getAddress());
			pst.setString(3, company.getEmail());
			pst.setString(4, company.getPhone());
			pst.setString(5, company.getFax());

			pst.executeUpdate();

			rs = pst.getGeneratedKeys();
			if (rs.next()) {
				recordId = rs.getLong(1); // id icin uretilen AI keyler
				company.setId(recordId);
			}

		} catch (Exception e) {
			logger.error("addcompany error:" + e.getMessage());
		} finally {
			closeResultSet(rs);
			closePreparedStatement(pst);
			closeConnection(con);
		}
		logger.debug("addCompany finished ID : " + recordId);
		return company;
	}

	private void checkSimilarCompanyRecord(Company company) throws Exception {
		logger.debug("checkSimilarCompanyRecord started");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		String query = "SELECT COMPANY_NAME, ID FROM companies WHERE COMPANY_NAME=?";

		try {
			con = getConnection();
			pst = con.prepareStatement(query);
			pst.setString(1, company.getName());

			rs = pst.executeQuery();
			if (rs.next()) {
				if (rs.getLong("ID") != company.getId())
					throw new WebApplicationException(409);
			}
		} catch (Exception e) {
			if (e instanceof WebApplicationException) {
				logger.error("checkSimilarCompanyRecord similar company founded!");
				throw e;
			} else {
				logger.error("checkSimilarCompanyRecord error:" + e.getMessage());
			}
		} finally {
			closeResultSet(rs);
			closePreparedStatement(pst);
			closeConnection(con);
			logger.debug("checkSimilarCompanyRecord finished");
		}
	}

	public ArrayList<Company> getAllcompanies() {
		logger.debug("getAllcompanies is started");

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		Company company;
		ArrayList<Company> companies = new ArrayList<Company>();

		try {
			String query = "SELECT * FROM companies";
			logger.debug("sql query created : " + query);
			con = getConnection();
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();

			while (rs.next()) {
				company = new Company();
				company.setId(rs.getLong("ID"));
				company.setName(rs.getString("COMPANY_NAME"));
				company.setEmail(rs.getString("EMAIL"));
				company.setPhone(rs.getString("PHONE"));
				company.setFax(rs.getString("FAX"));
				company.setAddress(rs.getString("ADDRESS"));
				companies.add(company);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			closeResultSet(rs);
			closePreparedStatement(pst);
			closeConnection(con);
		}
		logger.debug("getAllcompany finished. company # is " + companies.size());
		return companies;
	}

	public Company getCompany(long companyID) throws Exception {
		logger.debug("getCompany is started");

		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		Company company = null;

		String query = "SELECT * FROM companies WHERE ID=?";
		logger.debug("sql query created : " + query);

		try {
			con = getConnection();

			pst = con.prepareStatement(query);

			if (logger.isTraceEnabled()) {
				StringBuilder queryLog = new StringBuilder();
				queryLog.append("Query : ").append(query).append("\n");
				queryLog.append("Parameters : ").append("\n");
				queryLog.append("ID : ").append(companyID).append("\n");
				logger.trace(queryLog.toString());
			}
			pst.setLong(1, companyID);
			rs = pst.executeQuery();

			if (rs.next()) {
				company = new Company();
				company.setName(rs.getString("COMPANY_NAME"));
				company.setId(rs.getLong("ID"));
				company.setEmail(rs.getString("EMAIL"));
				company.setFax(rs.getString("FAX"));
				company.setPhone(rs.getString("PHONE"));
				company.setAddress(rs.getString("ADDRESS"));
			} else {
				throw new WebApplicationException(405);
			}
		} catch (Exception e) {
			logger.error("getCompany error : " + e.getMessage());
			if (e instanceof WebApplicationException)
				throw e;
		} finally {
			closeResultSet(rs);
			closePreparedStatement(pst);
			closeConnection(con);
		}
		logger.debug("getCompany finished");
		return company;
	}

	public void deleteCompany(long companyID) throws Exception {
		logger.debug("deleteCompanyData selectedCompanyID : " + companyID);

		// company de member varsa silme!
		if (isCompanyHasMember(companyID) == true) {
			throw new WebApplicationException(409);
		}
		Connection con = null;
		PreparedStatement pstCompany = null;
		StringBuilder queryDeleteCompany = new StringBuilder();

		try {
			queryDeleteCompany.append("DELETE FROM companies ");
			queryDeleteCompany.append("WHERE ID=?");
			String queryString = queryDeleteCompany.toString();
			logger.debug("sql query created : " + queryString);

			con = getConnection();
			pstCompany = con.prepareStatement(queryString);

			if (logger.isTraceEnabled()) {
				StringBuilder queryLog = new StringBuilder();
				queryLog.append("Query : ").append(queryString).append("\n");
				queryLog.append("Parameters : ").append("\n");
				queryLog.append("ID : ").append(companyID).append("\n");
				logger.trace(queryLog.toString());
			}

			pstCompany.setLong(1, companyID);
			pstCompany.executeUpdate();

		} catch (Exception e) {
			logger.error("error:" + e.getMessage());
		} finally {
			closePreparedStatement(pstCompany);
			closeConnection(con);
		}
		logger.debug("deleteCompany is finished");
	}

	// eger uye yoksa true, varsa false return edecek
	private boolean isCompanyHasMember(long companyID) throws Exception {
		logger.debug("isCompanyHasMember started");
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		boolean result = false;

		String query = "SELECT * FROM users WHERE COMPANY_ID=?";

		try {
			con = getConnection();
			pst = con.prepareStatement(query);
			pst.setLong(1, companyID);

			rs = pst.executeQuery();
			if (rs.next()) {
				result = true;
			}
		} catch (Exception e) {
			logger.error("isCompanyHasMember error:" + e.getMessage());
		} finally {
			closeResultSet(rs);
			closePreparedStatement(pst);
			closeConnection(con);
			logger.debug("isCompanyHasMember finished");
		}
		return result;
	}

	public void updateCompanyData(Company company) throws Exception {
		logger.debug("updateCompanyData is started");

		// ayni isimli company olamaz!
		checkSimilarCompanyRecord(company);

		Connection con = null;
		PreparedStatement pstUpdateUser = null;
		StringBuilder queryUpdateCompany = new StringBuilder();

		try {
			queryUpdateCompany.append("UPDATE companies SET ");
			queryUpdateCompany.append("COMPANY_NAME=?, ADDRESS=?, PHONE=?, FAX=?, EMAIL=? ");
			queryUpdateCompany.append("WHERE ID=? ;");
			String queryString = queryUpdateCompany.toString();
			logger.debug("sql query created : " + queryString);

			con = getConnection();
			pstUpdateUser = con.prepareStatement(queryString);

			pstUpdateUser.setString(1, company.getName());
			pstUpdateUser.setString(2, company.getAddress());
			pstUpdateUser.setString(3, company.getPhone());
			pstUpdateUser.setString(4, company.getFax());
			pstUpdateUser.setString(5, company.getEmail());
			pstUpdateUser.setLong(6, company.getId());
			pstUpdateUser.executeUpdate();

		} catch (Exception e) {
			logger.error("updateCompanyData error");
			e.printStackTrace();
		} finally {
			closePreparedStatement(pstUpdateUser);
			closeConnection(con);
		}
		logger.debug("updateCompanyData completed");
	}
}
