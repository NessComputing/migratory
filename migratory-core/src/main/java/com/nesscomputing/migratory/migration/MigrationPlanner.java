/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.migratory.migration;


import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.jgrapht.EdgeFactory;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nesscomputing.migratory.MigratoryException;
import com.nesscomputing.migratory.MigratoryException.Reason;

public class MigrationPlanner
{
    private static final Logger LOG = LoggerFactory.getLogger(MigrationPlanner.class);

    public enum MigrationDirection
    {
        UNPLANNED, FORWARD, BACK, DO_NOTHING;
    }

    private final MigrationManager migrationManager;
    private final String personalityName;

    private final int currentVersion;
    private final int requestedVersion;

    private boolean planned = false;

    private MigrationDirection migrationStrategy = MigrationDirection.UNPLANNED;
    private List<Migration> migrations = null;
    private int targetVersion = Integer.MIN_VALUE;

    private int firstVersion = Integer.MAX_VALUE;
    private int lastVersion = Integer.MIN_VALUE;

    public MigrationPlanner(final MigrationManager migrationManager,
                            final Integer currentVersion,
                            final Integer requestedVersion)
    {
        this.migrationManager = migrationManager;
        this.personalityName = migrationManager.getPersonalityName();

        this.currentVersion = currentVersion == null ? 0 : currentVersion;
        this.requestedVersion = requestedVersion == null ? 0 : requestedVersion;
    }

    public void plan()
    {
        if (!planned) {
            planned = true;

            final Map<String, Migration> availableMigrations = migrationManager.getMigrations();
            LOG.debug("Found {} available migrations, building migration graph", availableMigrations.size());

            // build a graph with the available migrations, prefer longer hops over shorter
            final WeightedGraph<Integer, MigrationEdge> graph = new DirectedWeightedMultigraph<Integer, MigrationEdge>(new MigrationEdgeFactory());

            // Add the migrations as vertexes and edges.
            for (Migration migration : availableMigrations.values()) {
                final int startVersion = migration.getStartVersion();
                final int endVersion = migration.getEndVersion();

                graph.addVertex(startVersion);
                graph.addVertex(endVersion);
                MigrationEdge edge = graph.addEdge(startVersion, endVersion);
                double weight = 1.0 / ((double) (startVersion - endVersion) * (startVersion - endVersion));
                graph.setEdgeWeight(edge, weight);


                edge.setMigration(migration);

                if (startVersion < firstVersion) {
                    firstVersion = startVersion;
                } else  if (startVersion > lastVersion) {
                    lastVersion = startVersion;
                }
                if (endVersion < firstVersion) {
                    firstVersion = endVersion;
                } else if (endVersion > lastVersion) {
                    lastVersion = endVersion;
                }
            }

            LOG.debug("Smallest Vertice: {}, Largest Vertice: {}", firstVersion, lastVersion);

            // find a way from "currentVersion" to "requestedVersion"
            // return the list
            this.targetVersion = (requestedVersion == Integer.MAX_VALUE) ? lastVersion : requestedVersion;

            if (!graph.containsVertex(currentVersion)) {
                throw new MigratoryException(Reason.VALIDATION_FAILED, "No starting point for personality '%s', version '%d' not found!", personalityName, currentVersion);
            }

            if (!graph.containsVertex(targetVersion)) {
                throw new MigratoryException(Reason.VALIDATION_FAILED, "No ending point for personality '%s', version '%d' not found!", personalityName, targetVersion);
            }

            final List<MigrationEdge> edges = new DijkstraShortestPath<Integer, MigrationEdge>(graph, currentVersion, targetVersion).getPathEdgeList();

            if (edges == null) {
                throw new MigratoryException(Reason.VALIDATION_FAILED, "No migration path for personality '%s', from '%d' to '%d' found!", personalityName, currentVersion, targetVersion);
            }

            if (currentVersion < targetVersion) {
                migrationStrategy = MigrationDirection.FORWARD;
            }
            else if (currentVersion > targetVersion) {
                migrationStrategy = MigrationDirection.BACK;
            }
            else {
                migrationStrategy = MigrationDirection.DO_NOTHING;
            }

            this.migrations = Lists.transform(edges, new Function<MigrationEdge, Migration>() {
                @Override
                public Migration apply(final MigrationEdge edge) {
                    return edge.getMigration();
                }
            });
        }
    }

    public MigrationDirection getDirection()
    {
        return migrationStrategy;
    }

    public List<Migration> getPlannedMigrations()
    {
        return migrations;
    }

    public int getTargetVersion()
    {
        return targetVersion;
    }

    public int getFirstVersion()
    {
        return firstVersion;
    }

    public int getLastVersion()
    {
        return lastVersion;
    }

    public String getPersonalityName()
    {
        return personalityName;
    }

    private static class MigrationEdgeFactory implements EdgeFactory<Integer, MigrationEdge>
    {
        @Override
        public MigrationEdge createEdge(final Integer sourceVertex, final Integer targetVertex)
        {
            return new MigrationEdge();
        }
    }

    private static class MigrationEdge extends DefaultWeightedEdge
    {
        private static final long serialVersionUID = 1L;

        private transient Migration migration;

        public Migration getMigration()
        {
            return migration;
        }

        private void setMigration(Migration migration)
        {
            this.migration = migration;
        }
    }

    @Override
    public String toString()
    {
        if (!planned) {
            return String.format("Personality '%s' not yet planned, requested %d to %d", personalityName, currentVersion, targetVersion);
        }

        final MigrationDirection md = getDirection();
        if (md == MigrationDirection.DO_NOTHING) {
            return String.format("Personality '%s' needs no migration, already at %d (Range: %d - %d)", personalityName, targetVersion, firstVersion, lastVersion);
        }
        else {
            return String.format("Personality '%s' migrates %s from %d to %d (Range: %d - %d)", personalityName, md == MigrationDirection.FORWARD ? "forward" : "backward", currentVersion, targetVersion, firstVersion, lastVersion);

        }
    }
}

