package cn.msqlite.queries;

public class Drop extends cn.msqlite.queries.QueryBuilder
{
	private boolean ifExists = false;
	
	public Drop(Class<?> type)
	{
		super(type);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String build()
	{
		StringBuilder builder = new StringBuilder("DROP TABLE ");
		if (ifExists) builder.append("IF EXISTS ");
		builder.append('`')
			.append(getTable().getName())
			.append('`')
			.append(';');
		return builder.toString();
	}
	
	public Drop setIfExists(boolean ifNotExists)
	{
		this.ifExists = ifNotExists;
		return this;
	}
	
}
