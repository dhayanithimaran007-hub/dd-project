package com.talentiq.service;

import com.talentiq.dao.AnalyticsDAO;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.util.Map;

public class AnalyticsService {

    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    public Map<String, Integer> getDashboardCounts() {
        return analyticsDAO.getDashboardCounts();
    }

    public CategoryDataset getExperienceDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> data = analyticsDAO.getExperienceDistribution();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Candidates", entry.getKey());
        }
        return dataset;
    }

    public CategoryDataset getApplicationsPerJobDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> data = analyticsDAO.getApplicationsPerJob();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Applications", entry.getKey());
        }
        return dataset;
    }

    public CategoryDataset getTopCandidateSkillsDataset(int limit) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> data = analyticsDAO.getTopCandidateSkills(limit);
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Candidate Skills", entry.getKey());
        }
        return dataset;
    }

    public CategoryDataset getTopDemandedSkillsDataset(int limit) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> data = analyticsDAO.getTopDemandedSkills(limit);
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Job Demands", entry.getKey());
        }
        return dataset;
    }

    public PieDataset<String> getMatchingScoreDataset() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<String, Integer> data = analyticsDAO.getMatchingScoreDistribution();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return dataset;
    }

    public PieDataset<String> getApplicationStatusDataset() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<String, Integer> data = analyticsDAO.getApplicationStatusBreakdown();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return dataset;
    }
}

