package util;


public class SimpleBean {
	
	public int id;
	
//	public SimpleBean(){
//		id = 5;
//	}

	private SimpleBean(int id){
		this.id = id;
	}

	public void printId(){
		System.out.println("ID= "+id);
	}

	public int getId() {
		return id;
	}
	
	public int incId(){
		return ++id;
	}

	public int decId(){
		return --id;
	}

	public static SimpleBean createInstance(){
		return new SimpleBean(10);
	}
	
}
