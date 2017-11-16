package com.borasoft.naqcc.sprint;

public class LogEntry {

	private String callsign;
	private String email;
	private String category;
	private String callArea;
	private String antenna;
	private String power;
	private String QSOs;
	private String memberQSOs;
	private String multipliers; 
	private String bonusMult; // float format
	private String soapbox;
	
	public boolean validate() {
		if(callsign==null || email==null || category==null || callArea==null
				|| antenna==null || power==null || QSOs==null || memberQSOs==null
				|| multipliers==null || bonusMult==null) {
			return false;
		} else {
			return true;
		}
	}
	
	public String getCallsign() {
		return callsign.toUpperCase();
	}
	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCallArea() {
		return callArea;
	}
	public void setCallArea(String callArea) {
		this.callArea = callArea;
	}
	public String getAntenna() {
		return normalize(antenna);
	}
	public void setAntenna(String antenna) {
		this.antenna = antenna;
	}
	public String getPower() {
		return power;
	}
	public void setPower(String power) {
		this.power = power;
	}
	public String getQSOs() {
		return QSOs;
	}
	public void setQSOs(String qSOs) {
		QSOs = qSOs;
	}
	public String getMemberQSOs() {
		return memberQSOs;
	}
	public void setMemberQSOs(String memberQSOs) {
		this.memberQSOs = memberQSOs;
	}
	public String getMultipliers() {
		return multipliers;
	}
	public void setMultipliers(String multipliers) {
		this.multipliers = multipliers;
	}
	public String getBonusMult() {
		return bonusMult;
	}
	public void setBonusMult(String bonusMult) {
		this.bonusMult = bonusMult;
	}
	public String getSoapbox() {
		return normalize(soapbox);
	}
	public void setSoapbox(String soapbox) {
		this.soapbox = soapbox;
	}
	
	public int getPoints() {
		// QSOs + MemberQSOs
		int points = 0;
		int qsos = Integer.parseInt(QSOs);
		int memberQsos = Integer.parseInt(memberQSOs);
		points = qsos + memberQsos;		
		return points;
	}

	public int getScore() {
		// points * multipliers
		int score = 0;
		int points = getPoints();
		int multi = Integer.parseInt(multipliers);
		score = points * multi;		
		return score;
	}
	
	public int getFinal() {
		int finalScore = getScore();
		if (!bonusMult.isEmpty()) {
			finalScore = (int)(finalScore * Float.parseFloat(bonusMult));
		}		
		return finalScore;
	}
	
	private String normalize(String s) {
	  // Remove '\'
	  String ns = s.replaceAll("[\\\\]","");
	  // Replace '&' with "&amp;"
	  ns = ns.replaceAll("[&]","&amp;");
	  // Replace '<' with "&LT;"
	  ns = ns.replaceAll("[<]","&lt;");
	  // Replace '>' with "&GT;"
	  ns = ns.replaceAll("[>]","&gt;");
	  return ns;
	}
}
