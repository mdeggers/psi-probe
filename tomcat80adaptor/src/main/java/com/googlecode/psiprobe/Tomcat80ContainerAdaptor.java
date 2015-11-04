/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */

package com.googlecode.psiprobe;

import com.googlecode.psiprobe.model.FilterInfo;
import com.googlecode.psiprobe.model.FilterMapping;

import org.apache.catalina.Context;
import org.apache.catalina.Valve;
import org.apache.catalina.WebResource;
import org.apache.naming.ContextAccessController;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * The Class Tomcat80ContainerAdaptor.
 *
 * @author Vlad Ilyushchenko
 * @author Mark Lewis
 * @author Andre Sollie
 */
public class Tomcat80ContainerAdaptor extends AbstractTomcatContainer {

  @Override
  protected Valve createValve() {
    return new Tomcat80AgentValve();
  }

  /**
   * Indicates whether this adapter can bind to the container.
   *
   * @param binding the ServerInfo of the container
   * @return true if binding is possible
   */
  @Override
  public boolean canBoundTo(String binding) {
    boolean canBind = false;
    if (binding != null) {
      canBind |= binding.startsWith("Apache Tomcat/8.0");
      canBind |= binding.startsWith("Apache Tomcat (TomEE)/8.0");
      canBind |= (binding.startsWith("Pivotal tc") && binding.contains("/8.0"));
    }
    return canBind;
  }

  /**
   * Gets the filter mappings.
   *
   * @param fmap the fmap
   * @param dm the dm
   * @param filterClass the filter class
   * @return the filter mappings
   */
  protected List<FilterMapping> getFilterMappings(FilterMap fmap, String dm, String filterClass) {
    String[] urls = fmap.getURLPatterns();
    String[] servlets = fmap.getServletNames();
    List<FilterMapping> results = new ArrayList<FilterMapping>(urls.length + servlets.length);
    for (String url : urls) {
      FilterMapping fm = new FilterMapping();
      fm.setUrl(url);
      fm.setFilterName(fmap.getFilterName());
      fm.setDispatcherMap(dm);
      fm.setFilterClass(filterClass);
      results.add(fm);
    }
    for (String servlet : servlets) {
      FilterMapping fm = new FilterMapping();
      fm.setServletName(servlet);
      fm.setFilterName(fmap.getFilterName());
      fm.setDispatcherMap(dm);
      fm.setFilterClass(filterClass);
      results.add(fm);
    }
    return results;
  }

  @Override
  public List<FilterMapping> getApplicationFilterMaps(Context context) {
    FilterMap[] fms = context.findFilterMaps();
    List<FilterMapping> filterMaps = new ArrayList<FilterMapping>(fms.length);
    for (FilterMap filterMap : fms) {
      if (filterMap != null) {
        String dm;
        switch (filterMap.getDispatcherMapping()) {
          case FilterMap.ERROR:
            dm = "ERROR";
            break;
          case FilterMap.FORWARD:
            dm = "FORWARD";
            break;
          // case FilterMap.FORWARD_ERROR: dm = "FORWARD,ERROR"; break;
          case FilterMap.INCLUDE:
            dm = "INCLUDE";
            break;
          // case FilterMap.INCLUDE_ERROR: dm = "INCLUDE,ERROR"; break;
          // case FilterMap.INCLUDE_ERROR_FORWARD: dm = "INCLUDE,ERROR,FORWARD"; break;
          // case FilterMap.INCLUDE_FORWARD: dm = "INCLUDE,FORWARD"; break;
          case FilterMap.REQUEST:
            dm = "REQUEST";
            break;
          // case FilterMap.REQUEST_ERROR: dm = "REQUEST,ERROR"; break;
          // case FilterMap.REQUEST_ERROR_FORWARD: dm = "REQUEST,ERROR,FORWARD"; break;
          // case FilterMap.REQUEST_ERROR_FORWARD_INCLUDE: dm = "REQUEST,ERROR,FORWARD,INCLUDE";
          // break;
          // case FilterMap.REQUEST_ERROR_INCLUDE: dm = "REQUEST,ERROR,INCLUDE"; break;
          // case FilterMap.REQUEST_FORWARD: dm = "REQUEST,FORWARD"; break;
          // case FilterMap.REQUEST_INCLUDE: dm = "REQUEST,INCLUDE"; break;
          // case FilterMap.REQUEST_FORWARD_INCLUDE: dm = "REQUEST,FORWARD,INCLUDE"; break;
          default:
            dm = "";
        }

        String filterClass = "";
        FilterDef fd = context.findFilterDef(filterMap.getFilterName());
        if (fd != null) {
          filterClass = fd.getFilterClass();
        }

        List<FilterMapping> filterMappings = getFilterMappings(filterMap, dm, filterClass);
        filterMaps.addAll(filterMappings);
      }
    }
    return filterMaps;
  }

  @Override
  public List<FilterInfo> getApplicationFilters(Context context) {
    FilterDef[] fds = context.findFilterDefs();
    List<FilterInfo> filterDefs = new ArrayList<FilterInfo>(fds.length);
    for (FilterDef filterDef : fds) {
      if (filterDef != null) {
        FilterInfo fi = getFilterInfo(filterDef);
        filterDefs.add(fi);
      }
    }
    return filterDefs;
  }

  /**
   * Gets the filter info.
   *
   * @param fd the fd
   * @return the filter info
   */
  private static FilterInfo getFilterInfo(FilterDef fd) {
    FilterInfo fi = new FilterInfo();
    fi.setFilterName(fd.getFilterName());
    fi.setFilterClass(fd.getFilterClass());
    fi.setFilterDesc(fd.getDescription());
    return fi;
  }

  @Override
  public boolean resourceExists(String name, Context context) {
    return context.getResources().getResource(name) != null;
  }

  @Override
  public InputStream getResourceStream(String name, Context context) throws IOException {
    WebResource resource = context.getResources().getResource(name);
    return resource.getInputStream();
  }

  @Override
  public Long[] getResourceAttributes(String name, Context context) {
    Long[] result = new Long[2];
    WebResource resource = context.getResources().getResource(name);
    result[0] = resource.getContentLength();
    result[1] = resource.getLastModified();
    return result;
  }

  /**
   * Returns the security token required to bind to a naming context.
   *
   * @param context the catalina context
   *
   * @return the security token for use with <code>ContextBindings</code>
   */
  @Override
  protected Object getNamingToken(Context context) {
    // null token worked before 8.0.6
    Object token = null;
    if (!ContextAccessController.checkSecurityToken(context, token)) {
      // namingToken added to Context and Server interfaces in 8.0.6
      // Used by NamingContextListener when settinp up JNDI context
      token = context.getNamingToken();
      if (!ContextAccessController.checkSecurityToken(context, token)) {
        logger.error("Couldn't get a valid security token. ClassLoader binding will fail.");
      }
    }
    return token;
  }

}
