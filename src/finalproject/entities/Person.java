package finalproject.entities;

public class Person implements java.io.Serializable {

	private static final long serialVersionUID = 4190276780070819093L;

	// this is a person object that you will construct with data from the DB
	// table. The "sent" column is unnecessary. It's just a person with
	// a first name, last name, age, city, and ID.
	
	private String first;
	private String last;
	private int age;
	private String city;
	private boolean sent;
	private int id;
	
	public Person(String first, String last, int age, String city, boolean sent, int id) {
		this.first = first;
		this.last = last;
		this.age = age;
		this.city = city;
		this.sent = sent;
		this.id = id;
	}
	
	public String getFirst() {
		return first;
	}
	
	public String getLast() {
		return last;
	}
	
	public int getAge() {
		return age;
	}
	
	public String getCity() {
		return city;
	}
	
	public boolean getSent() {
		return sent;
	}
	
	public int getId() {
		return id;
	}
	
	public String toString() {
		return "Person [first=" + first + " last=" + last + " age=" + age + " city=" + city + " id=" + id + "]\n";
	}
}
