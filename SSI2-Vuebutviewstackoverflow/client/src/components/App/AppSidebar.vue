<script setup></script>

<template>
  <nav id="app-sidebar">
    <img id="app-sidebar-logo" src="../../assets/logo.png" alt="Nav Logo" />
    <ul id="navs">
      <li>
        <router-link :to="{ path: '/' }">
          <font-awesome-icon icon="calendar-days" />
          <span>Schedules</span>
        </router-link>
      </li>
      <li>
        <router-link
          :to="{ path: '/categories' }"
          :class="$route.path.includes('/categories') && 'router-link-active'"
        >
          <font-awesome-icon icon="list" />
          <span>Categories</span>
        </router-link>
      </li>
    </ul>
  </nav>
</template>

<style scoped>
#app-sidebar {
  width: 100%;
  background-color: #13293d;
}

#app-sidebar-logo {
  display: block;
  margin: 0 auto;
  width: 180px;
  padding: 20px;
}

#navs {
  margin: 0 auto;
  list-style: none;
}

/*select router-link*/
#navs li a {
  display: block;
  padding: 8px;
  text-decoration: none;
  color: white;
  transition: all 0.3s ease-out;
  opacity: 0.5;
}

#navs li a:hover {
  opacity: 0.8;
}

#navs li a.router-link-active,
#navs li a.router-link-exact-active {
  opacity: 1;
  background-color: #006494;
}

#navs li a svg {
  width: 20px;
}

#navs li a span {
  padding: 0 12px;
}
</style>
