// ------------------------------------------------------
// Manually coded Promela functions
// ------------------------------------------------------


inline Test_myGuard(retVal)
{
  if 
  :: retVal = true
  :: retVal = false
  fi
}

inline Test_s1Enter()
{
  printf("Test_s1Enter()\n");
}

inline Test_s1Exit()
{
  printf("Test_s1Exit()\n");
}

inline Test_s2Enter()
{
  printf("Test_s2Enter()\n");
}

inline Test_s2Exit()
{
  printf("Test_s2Exit()\n");
}

inline Test_s3Enter()
{
  printf("Test_s3Enter()\n");
}
