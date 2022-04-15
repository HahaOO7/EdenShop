package at.haha007.edenshop;

public class ShopState {

	enum State {NORMAL, EIGENE, AUSGELAUFENE, ANDERE}
	
	State state;
	private int seite;
	boolean opFilter = false;
	boolean opAuslaufen = false;
	int amount = 1;
	MaterialFilter.Filter filter;
	
	ShopState(State state, int seite, MaterialFilter.Filter filter) {
		this.state = state;
		this.seite = seite;
		this.filter = filter;
	}
	
	void changeAmount() {
		
		if (amount == 1) amount = 5;
		else if (amount == 5) amount = 10;
		else if (amount == 10) amount = 32;
		else if (amount == 32) amount = 64;
		else if (amount == 64) amount = 1;
		
	}
	
	int getPage() {return seite;}
	
	void next() { seite++; }
	void prev() { if (seite > 0) seite--; }
	
}
