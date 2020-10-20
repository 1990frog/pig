package com.clinbrain.dip.strategy.sqlparse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Liaopan on 2020-10-13.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TableColumnItem {

	private String tableAliasName;

	private String columnName;



}
