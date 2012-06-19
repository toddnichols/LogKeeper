import java.util.Date;


public class TALKAppointment {
	private int id;
	private String firstName;
	private String lastName;
	private String skypeName;
	private Date time;
	private String status;
	
	public TALKAppointment(
			int id, 
			String firstName, 
			String lastName, 
			String skypeName, 
			Date time,
			String status
	){
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.skypeName = skypeName;
		this.time = time;
		this.status = status;
	}
	
	public int getId(){
		return id;
	}
	
	public String getFirstName(){
		return firstName;
	}
	
	public String getLastName(){
		return lastName;
	}
	
	public String getFullName(){
		StringBuilder name = new StringBuilder();
		name.append(firstName).append(" ").append(lastName);
		return name.toString();
	}
	
	public String getSkypeName(){
		return skypeName;
	}
	
	public Date getTime(){
		return time;
	}
	
	public String getStatus(){
		return status;
	}
}
