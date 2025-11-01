package com.aps.hino.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
	@Id
	private Long id;

	/** resource type: users, vehicles, maintenance, cotizaciones, etc. */
	private String entity;

	/** id of the resource affected (nullable) */
	@Column("entity_id")
	private Long entityId;

	/** action performed: CREATE | UPDATE | DELETE | OTHER */
	private String action;

	/** human friendly description */
	private String description;

	/** id of actor (if available) */
	@Column("actor_id")
	private Long actorId;

	/** actor email or username (if available) */
	@Column("actor_email")
	private String actorEmail;

	@Column("is_read")
	private Boolean read = false;

	@Column("created_at")
	private OffsetDateTime createdAt;
}
