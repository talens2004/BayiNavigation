package cn.msqlite.queries;

import android.util.Log;

import java.util.List;

import cn.msqlite.models.Column;
import cn.msqlite.models.Table;

public class CreateTable extends cn.msqlite.queries.QueryBuilder
{
	private boolean 
		IF_NOT_EXISTS = false;

	public CreateTable(Class<?> type)
	{
		super(type);
	}
	
	public CreateTable(Table table)
	{
		super(table);
	}

	public CreateTable setIF_NOT_EXIST(boolean iF_NOT_EXIST)
	{
		IF_NOT_EXISTS = iF_NOT_EXIST;
		return this;
	}
	
	@Override
	public String build()
	{
		Table table = getTable();
		
		StringBuilder builder = new StringBuilder("CREATE TABLE ");
		if (IF_NOT_EXISTS) builder.append(" IF NOT EXISTS");
		builder
			.append('`')
			.append(table.getName())
			.append('`')
			.append(" (");
		
		int n=0;
		for (Column column : table.getColumns())
		{
			if (n++ > 0) builder.append(',');
			builder
				.append('\n')
				.append(column.getBuilder());
		}
		
		n=0;
		List<Column> primaryKeys = table.getPrimaryKeys();
		if (primaryKeys.isEmpty() == false)
		{
			builder.append(",\nPRIMARY KEY (");
			for (Column column : primaryKeys)
			{
				if (n++ > 0) builder.append(", ");
				builder.append('`').append(column.getName()).append('`');
			}
			builder.append(")");
		}
		builder.append("\n);");
		Log.d("QueryBuilder", builder.toString());
		return builder.toString();
	}
	
}
