package agent;

import java.util.HashMap;
import java.util.Map.Entry;


public class Options {
	HashMap<String, Object> map = new HashMap<String, Object>();

    public Options copy()
    {
        Options opt = new Options();
        opt.map.putAll(this.map);
        return opt;
    }

	public void put(String name, Object value)
	{
		map.put(name, value);
	}
	
	public Object get(String name)
	{
		Object val = map.get(name);
		if (val==null)
			System.err.println("option '"+name+"' not found");
		return val;
	}
	
	public Double getDouble(String name)
	{
		Object val = get(name);
		if (Double.class.isInstance(val))
			return (Double) val;
		throw new RuntimeException("option '"+name+"' is not double");
	}

	public Integer getInteger(String name)
	{
		Object val = get(name);
		if (Integer.class.isInstance(val))
			return (Integer) val;
		throw new RuntimeException("option '"+name+"' is not integer");
	}

    @Override
    public String toString()
    {
        String s = "";
        s += map.size() + "\n";
        for (Entry<String, Object> e : map.entrySet())
        {
            s += e.getKey() + " = " + e.getValue() + "\n";
        }
        return s;
    }
}
